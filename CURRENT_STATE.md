# PDA App - Current State Documentation

## Overview
The PDA app is a task management application with a post-apocalyptic theme. It allows users to create, organize, and complete tasks while earning XP and receiving feedback through an NPC message system.

## Core Components

### Task Management
- Tasks can be created with titles, descriptions, categories, and difficulty levels
- Tasks support due dates and times for scheduling
- Subtask functionality allows complex task breakdown
- Tasks can be marked as complete, earning XP based on difficulty
- Add task button repositioned next to "Active Tasks" header for better UX

### User System
- User profile with level progression
- XP earned for completing tasks (10-100 XP based on difficulty)
- Category-based statistics tracking completion rates
- Category rank system with thematic level names

### Notification System
- Precise time-based notifications delivered at exact due times
- Uses Android's AlarmManager with wake locks to ensure delivery
- Notification styling based on task category
- Permission handling for Android 13+ notification requirements
- Sound effects for task interactions

### NPC Interaction
- NPCs provide themed feedback messages for task completion/failure
- Messages are category-specific with NPCs assigned to different areas
- Unread message tracking with badge indicators
- Expanded dialogue options for more varied NPC interactions
- Shopping category NPCs (Grifter and Viktor "The Mule") received additional dialogue lines

### UI Structure
- Main screen displays active tasks with completion controls
- Menu navigation to specialized screens:
  - Completed tasks archive
  - Category statistics and progress
  - NPC message inbox
- Theme customization options
- Full dark mode support (system default, light, and dark options)
- Refined animation system using native Compose animations

## Technical Implementation

### Core Architecture
- Built with Kotlin for Android
- UI constructed with Jetpack Compose
- State management through StateFlow 
- Repository pattern for data management
- Native Compose animations for visual feedback

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
- `CategoryRank`: Provides thematic progression system

### Animation System
- Replaced external libraries with native Compose animations
- Implemented simple rotation effect for "Study" category tasks
- Optimized animation performance for smoother transitions
- Simplified animation system for better maintainability

## Current Limitations
- Limited animation variety with category-specific animations for "Study" only
- Local-only storage without cloud synchronization
- Limited visual feedback for some user interactions
- Standard notification design without rich interaction capabilities

## Performance Considerations
- Notification system is optimized for precise timing
- Error handling and logging implemented throughout
- Battery usage considerations for background processes
- Animations are lightweight and non-blocking
- Dark mode implementation reduces power usage on OLED screens

## Recent Fixes
- Repositioned add task button for improved UX
- Implemented simplified paper folding animation effect for "Study" tasks
- Expanded NPC dialogue options for shopping category
- Fixed animation implementation issues using native Compose animations
- Improved UI consistency and interaction feedback

## Future Plans
- Potential integration with AI APIs for dynamic NPC dialogue generation
- Enhanced animation system with more category-specific effects
- Improved visual feedback for user interactions
- Additional task organization features
- Extended NPC dialogue system and interaction options

## Documentation
- **FEATURES.md**: Contains the feature roadmap and implementation status
- **RECENT_FIXES.md**: Tracks the latest bug fixes and improvements
- **FUTURE.md**: Documents future development plans and architectural decisions

*Last Updated: June 20, 2024* 