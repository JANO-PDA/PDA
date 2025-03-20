# Future Development Plans

## Animation System Roadmap

### Phase 1: Foundation (Current)
- **Native Compose Animations**: Use built-in Compose animations for all UI feedback
- **Performance Optimization**: Focus on ensuring animations are lightweight and non-blocking
- **Consistent UX**: Ensure a uniform animation experience across the application
- **Category-Specific Animations**: Current implementation includes a paper folding animation for "Study" tasks

### Phase 2: Enhanced Visual Feedback (Planned)
- **Additional Category Animations**: Implement unique animations for each task category
- **Task Completion Animations**: Add satisfaction-inducing animations when tasks are marked complete
- **Refined Transitions**: Implement smoother transitions between app states and screens
- **Loading Indicators**: Create themed loading animations for background operations

### Phase 3: User Experience Improvements (Future)
- **Ambient Animations**: Subtle background movements to enhance the post-apocalyptic atmosphere
- **Interaction Feedback**: Micro-animations for buttons, sliders, and other interactive elements
- **Animation Settings**: Allow users to control animation intensity or disable animations

### Decision Notes
- **Native Animations Over Libraries**: The decision to use native Compose animations instead of Lottie or other animation libraries prioritizes:
  - Smaller App Size: No additional animation assets or libraries needed
  - Maintainability: Simpler codebase with fewer dependencies to manage
  - Performance: Reduced resource usage and simpler state management
  - Consistency: Unified visual language across the application
- **Category-Specific Animations**: Started with a paper folding/rotation effect for the "Study" category, with plans to expand to other categories.

## NPC Dialogue Enhancement Plan

### Phase 1: Expanded Static Content (Current)
- **Diverse Dialogue Lines**: Added more varied conversation options for existing NPCs
- **Character Development**: Expanded personality traits through dialogue
- **Shopping Category Focus**: Enhanced dialogue for Grifter and Viktor "The Mule" NPCs

### Phase 2: Dynamic Content Generation (Planned)
- **API Integration Research**: Investigate free tier options for AI dialogue generation
  - Hugging Face Inference API (free tier)
  - Google Gemini API (free tier with limitations)
  - OpenAI API (limited free tier)
- **Hybrid Approach**: Combine pre-written lines with API-generated content
- **Offline Fallback**: Ensure app functions properly when API is unavailable

### Phase 3: Advanced NPC Interaction (Future)
- **Context-Aware Responses**: Dialogue that references user's task history and patterns
- **Adaptive Personalities**: NPCs that evolve based on user interaction frequency
- **Expandable NPC Roster**: System for introducing new characters with unique traits

## UI Enhancement Roadmap

### Planned Improvements
- **Task Button Positioning**: Completed repositioning of add task button beside "Active Tasks" header
- **Dark Mode**: Full support for system and manual dark mode toggle
- **Accessibility Enhancements**: Improve screen reader support and tap target sizes
- **Responsive Layouts**: Better adaptation to different screen sizes and orientations
- **Gesture Navigation**: Add swipe actions for common task operations

### Visual Design Evolution
- **Refined Theme**: Enhance the post-apocalyptic visual identity
- **Custom Iconography**: Develop a consistent icon set that fits the theme
- **Typography Review**: Optimize readability while maintaining thematic elements

## Technical Roadmap

### Architecture Improvements
- **Modularization**: Break monolithic structure into feature modules
- **Testing Coverage**: Expand unit and UI test coverage
- **State Management**: Further refinement of state flow patterns

### Performance Goals
- **Startup Time**: Optimize application launch experience
- **Memory Usage**: Reduce overall memory footprint
- **Battery Impact**: Minimize background processing requirements
- **Dependencies**: Continue to minimize external dependencies where native solutions exist

*Last Updated: June 20, 2024* 