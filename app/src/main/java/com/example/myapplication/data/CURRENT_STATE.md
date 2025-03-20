# PDA App - Current State Documentation

## Overview
The PDA app is a task management application with a post-apocalyptic theme. It allows users to create, organize, and complete tasks while earning XP and receiving feedback through an NPC message system.

## Core Components

### Task Management
- Tasks can be created with titles, descriptions, categories, and difficulty levels
- Tasks support due dates and times for scheduling
- Subtask functionality allows complex task breakdown
- Tasks can be marked as complete, earning XP based on difficulty

### User System
- User profile with level progression
- XP earned for completing tasks (10-100 XP based on difficulty)
- Category-based statistics tracking completion rates

### Notification System
- Precise time-based notifications delivered at exact due times
- Uses Android's AlarmManager with wake locks to ensure delivery
- Notification styling based on task category
- Permission handling for Android 13+ notification requirements

### NPC Interaction
- NPCs provide themed feedback messages for task completion/failure
- Messages are category-specific with NPCs assigned to different areas
- Unread message tracking with badge indicators

### UI Structure
- Main screen displays active tasks with completion controls
- Menu navigation to specialized screens:
  - Completed tasks archive
  - Category statistics and progress
  - NPC message inbox
- Theme customization options
- Task creation animations and visual feedback for user actions

## Technical Implementation

### Core Architecture
- Built with Kotlin for Android
- UI constructed with Jetpack Compose
- State management through StateFlow 
- Repository pattern for data management
- Lottie for high-quality animations

### Key Classes
- `TodoViewModel`: Central coordinator for app functionality
- `AlarmScheduler`: Manages notification timing and delivery
- `NotificationHelper`: Creates and styles notifications
- `TaskAlarmReceiver`: Handles alarm broadcasts
- `NpcRepository`: Manages NPC message generation and storage

### Data Models
- `Task`: Core data structure with metadata and utility methods
- `UserProfile`: Tracks user progress and preferences
- `NpcMessage`: Stores communication from NPCs
- `TaskCategory` and `TaskDifficulty`: Enumeration types

## Current Limitations
- Basic animation system without thematic elements
- Local-only storage without cloud synchronization
- Limited visual feedback for user interactions
- Standard notification design without rich interaction capabilities

## Performance Considerations
- Notification system is optimized for precise timing
- Error handling and logging implemented throughout
- Battery usage considerations for background processes

## Documentation
- **FEATURES.md**: Contains the feature roadmap and implementation status
- **RECENT_FIXES.md**: Tracks the latest bug fixes and improvements
- **CODE_STRUCTURE.md**: Documentation on code organization (planned)

*Last Updated: May 26, 2024* 