package com.example.balancebuddy.services;

import com.example.balancebuddy.dtos.GoalRequestDTO;
import com.example.balancebuddy.entities.Goal;
import com.example.balancebuddy.entities.MyUser;
import com.example.balancebuddy.exceptions.GoalNotFoundException;
import com.example.balancebuddy.exceptions.HabitNotFoundException;
import com.example.balancebuddy.exceptions.UserNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GoalService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    UserService userService;

    @Transactional
    public List<Goal> getAllGoals() {
        return entityManager.createQuery("SELECT g from Goal g", Goal.class).getResultList();
    }

    @Transactional
    public Goal createGoal(GoalRequestDTO createGoalRequest) {
        // Use UserService to find the user by ID
        Optional<MyUser> userOptional = userService.findByID(createGoalRequest.getUserID());
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException(createGoalRequest.getUserID());
        }

        MyUser user = userOptional.get();

        Goal goal = Goal.builder()
                .user(user)
                .periodicity(createGoalRequest.getPeriodicity())
                .target(createGoalRequest.getTarget())
                .habits(createGoalRequest.getHabits())
                .progress(createInitialProgress(createGoalRequest.getHabits()))
                .build();

        entityManager.persist(goal);
        return goal;
    }

    @Transactional
    public Optional<Goal> findGoalByID(int id) {
        Goal goal = entityManager.find(Goal.class, id);
        if (goal == null) {
            throw new GoalNotFoundException(id);
        }
        return Optional.of(goal);
    }

    @Transactional
    public Goal updateGoal(int id, Goal updatedGoal) {
        Goal existingGoal = entityManager.find(Goal.class, id);
        if (existingGoal == null) {
            throw new GoalNotFoundException(id);
        }

        existingGoal.setPeriodicity(updatedGoal.getPeriodicity());
        existingGoal.setTarget(updatedGoal.getTarget());
        existingGoal.setHabits(updatedGoal.getHabits());
        existingGoal.setProgress(createInitialProgress(updatedGoal.getHabits()));

        existingGoal.setUser(existingGoal.getUser());
        return entityManager.merge(existingGoal);
    }

    @Transactional
    public void deleteGoal(int id) {
        Goal goal = entityManager.find(Goal.class, id);
        if (goal != null) {
            entityManager.remove(goal);
        } else {
            throw new GoalNotFoundException(id);
        }
    }

    @Transactional
    public Goal addHabitToGoal(int goalID, String habitName, String targetValue) {
        Goal goal = entityManager.find(Goal.class, goalID);
        if (goal != null) {
            String currentHabits = goal.getHabits();
            String currentProgress = goal.getProgress();
            if (currentHabits == null || currentHabits.isEmpty()) {
                goal.setHabits(habitName + ";");
                goal.setProgress("0;");
            } else {
                goal.setHabits(currentHabits + ";" + habitName);
                goal.setProgress(currentProgress + ";0");
            }

            String currentTargetValues = goal.getTarget();
            if (currentTargetValues == null || currentTargetValues.isEmpty()) {
                goal.setTarget(targetValue + ";");
            } else {
                goal.setTarget(currentTargetValues + ";" + targetValue);
            }

            entityManager.merge(goal);
        } else {
            throw new GoalNotFoundException(goalID);
        }
        return goal;
    }

    // Method to update user's progress based on chosen habit
    @Transactional
    public void updateProgress(int goalID, String habitName, int progressValue) {
        Goal goal = entityManager.find(Goal.class, goalID);
        if (goal != null) {
            // Separate the elements from the habits and progress strings
            String[] habits = goal.getHabits().split(";");
            String[] progress = goal.getProgress().split(";");

            boolean habitFound = false; // Flag to check if habit is found

            for (int i = 0; i < habits.length; i++) {
                // If the habitName is the same with the current habit in the string, update the progress
                if (habitName.equals(habits[i])) {
                    int currentProgress = Integer.parseInt(progress[i]);
                    // Add the input value to the current value in the progress String
                    progress[i] = String.valueOf(currentProgress + progressValue);
                    goal.setProgress(String.join(";", progress));
                    entityManager.merge(goal);
                    habitFound = true;
                    break;
                }
            }

            if (!habitFound) {
                throw new HabitNotFoundException(habitName);
            }
        } else {
            throw new GoalNotFoundException(goalID);
        }
    }


    // Method to initialize the progress for all habits with 0
    private String createInitialProgress(String habits) {
        String[] habitArray = habits.split(";");
        StringBuilder progressBuilder = new StringBuilder();
        for (int i = 0; i < habitArray.length; i++) {
            progressBuilder.append("0");
            if (i < habitArray.length - 1) {
                progressBuilder.append(";");
            }
        }
        return progressBuilder.toString();
    }
}
