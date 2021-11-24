package main

import (
	"context"
	"log"
	"net"
	"sort"

	"do9core.xyz/proto-todo-server/api"

	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
)

func main() {

	listener, err := net.Listen("tcp", ":6672")
	if err != nil {
		log.Fatalf("Acquire port failed: %v", err)
		return
	}

	log.Println("Starting server")

	initialItems := map[int64]*api.TodoDetail{
		1: {
			Id:          1,
			Title:       "Buy Potatoes",
			Description: "Buy potatoes for dinner",
			Steps:       make([]*api.TodoStep, 0),
			Completed:   false},
		2: {
			Id:          2,
			Title:       "Do Experiment",
			Description: "Try to finish gRPC experiment project",
			Steps: []*api.TodoStep{
				{
					Id:          1,
					Title:       "Create protocol buffers",
					Description: "Create protocol buffers for gRPC",
				},
				{
					Id:          2,
					Title:       "Create gRPC server",
					Description: "Create gRPC server",
				},
				{
					Id:          3,
					Title:       "Create gRPC client",
					Description: "Create gRPC client",
				},
			},
			Completed: false},
		3: {
			Id:          3,
			Title:       "Read the book",
			Description: "Rust Programming Language",
			Steps:       make([]*api.TodoStep, 0),
			Completed:   true},
	}

	grpcServer := grpc.NewServer()

	api.RegisterTodoServiceServer(grpcServer, &TodoGrpcServer{initialItems})

	reflection.Register(grpcServer)

	err = grpcServer.Serve(listener)
	if err != nil {
		log.Fatalf("Server starting failed: %v", err)
		return
	}
}

type TodoGrpcServer struct {
	cache map[int64]*api.TodoDetail
}

func (s *TodoGrpcServer) ListTodos(
	ctx context.Context,
	req *api.ListTodoRequest) (*api.ListTodoResponse, error) {
	var summaryList []*api.TodoSummary
	for _, todo := range s.cache {
		summary := &api.TodoSummary{
			Id:        todo.Id,
			Title:     todo.Title,
			Completed: todo.Completed}
		summaryList = append(summaryList, summary)
	}

	sort.Slice(summaryList, func(i, j int) bool {
		return summaryList[i].Id < summaryList[j].Id
	})

	resp := &api.ListTodoResponse{Todos: summaryList}
	return resp, nil
}

func (s *TodoGrpcServer) CompleteTodo(
	ctx context.Context,
	req *api.CompleteTodoRequest) (*api.CompleteTodoResponse, error) {
	s.cache[req.Id].Completed = true
	return &api.CompleteTodoResponse{}, nil
}

func (s *TodoGrpcServer) DeCompleteTodo(
	ctx context.Context,
	req *api.DeCompleteTodoRequest) (*api.DeCompleteTodoResponse, error) {
	detail := s.cache[req.Id]
	if detail != nil {
		detail.Completed = false
	}
	return &api.DeCompleteTodoResponse{}, nil
}

func (s *TodoGrpcServer) UpdateTodo(
	ctx context.Context,
	req *api.UpdateTodoRequest) (*api.UpdateTodoResponse, error) {
	detail := s.cache[req.Id]
	if detail != nil {
		if req.Title != nil {
			detail.Title = *req.Title
		}
		if req.Description != nil {
			detail.Description = *req.Description
		}
	}
	return &api.UpdateTodoResponse{Todo: detail}, nil
}

func (s *TodoGrpcServer) TodoDetail(
	ctx context.Context,
	req *api.TodoDetailRequest) (*api.TodoDetailResponse, error) {
	return &api.TodoDetailResponse{Todo: s.cache[req.Id]}, nil
}

func (s *TodoGrpcServer) CompleteTodoStep(
	ctx context.Context,
	req *api.CompleteTodoStepRequest) (*api.CompleteTodoStepResponse, error) {
	todo := s.cache[req.TodoId]
	if todo != nil {
		for _, step := range todo.Steps {
			if step.Id == req.StepId {
				step.Completed = true
				break
			}
		}
	}
	return &api.CompleteTodoStepResponse{}, nil
}

func (s *TodoGrpcServer) DeCompleteTodoStep(
	ctx context.Context,
	req *api.DeCompleteTodoStepRequest) (*api.DeCompleteTodoStepResponse, error) {
	todo := s.cache[req.TodoId]
	if todo != nil {
		for _, step := range todo.Steps {
			if step.Id == req.StepId {
				step.Completed = false
				break
			}
		}
	}
	return &api.DeCompleteTodoStepResponse{}, nil
}

func (s *TodoGrpcServer) UpdateTodoStep(
	ctx context.Context,
	req *api.UpdateTodoStepRequest) (*api.UpdateTodoStepResponse, error) {
	var step *api.TodoStep
	todo := s.cache[req.TodoId]
	if todo != nil {
		for _, step := range todo.Steps {
			if step.Id == req.StepId {
				if req.Title != nil {
					step.Title = *req.Title
				}
				if req.Description != nil {
					step.Description = *req.Description
				}
				break
			}
		}
	}
	return &api.UpdateTodoStepResponse{Step: step}, nil
}

func (s *TodoGrpcServer) CreateTodo(
	ctx context.Context,
	req *api.CreateTodoRequest) (*api.CreateTodoResponse, error) {
	var nextId int64 = 0
	for k := range s.cache {
		if k > nextId {
			nextId = k + 1
		}
	}
	newTodo := &api.TodoDetail{
		Id:          nextId,
		Title:       req.Title,
		Description: req.Description,
		Steps:       req.Steps,
		Completed:   false}
	s.cache[nextId] = newTodo
	return &api.CreateTodoResponse{Todo: newTodo}, nil
}

func (s *TodoGrpcServer) DeleteTodo(
	ctx context.Context,
	req *api.DeleteTodoRequest) (*api.DeleteTodoResponse, error) {
	delete(s.cache, req.Id)
	return &api.DeleteTodoResponse{}, nil
}

func (s *TodoGrpcServer) CreateTodoStep(
	ctx context.Context,
	req *api.CreateTodoStepRequest) (*api.CreateTodoStepResponse, error) {
	var todo *api.TodoDetail
	var step *api.TodoStep
	for _, t := range s.cache {
		if t.Id == req.TodoId {
			todo = t
			break
		}
	}
	if todo != nil {
		var nextId = 0
		for i := range todo.Steps {
			if i > nextId {
				nextId = i + 1
			}
		}
		step = &api.TodoStep{
			Id:          int64(nextId),
			Title:       req.Title,
			Description: req.Description,
			Completed:   false}
		todo.Steps = append(todo.Steps, step)
	}
	return &api.CreateTodoStepResponse{Step: step}, nil
}

func (s *TodoGrpcServer) DeleteTodoStep(
	ctx context.Context,
	req *api.DeleteTodoStepRequest) (*api.DeleteTodoStepResponse, error) {
	var todo *api.TodoDetail
	for _, t := range s.cache {
		if t.Id == req.TodoId {
			todo = t
			break
		}
	}
	if todo != nil {
		var steps []*api.TodoStep
		for _, step := range todo.Steps {
			if step.Id != req.StepId {
				steps = append(steps, step)
			}
		}
		todo.Steps = steps
	}
	return &api.DeleteTodoStepResponse{}, nil
}
