# 📱 My Goals - Health Tracker (Android App Prompt)

## 🧾 Overview
Create a production-ready Android app named **"My Goals - Health Tracker"** that allows users to:
- Track daily meals and water intake
- Create structured diet plans with multiple meal options
- Log actual food consumption based on selected options
- View nutritional progress (calories & macros)
- Get reminders before meals
- Export and import all data via JSON

---

## 🧱 Tech Stack

- Language: Kotlin
- UI: Jetpack Compose
- Design System: Material 3
- Architecture: MVVM + Repository Pattern
- Database: Room
- Background Tasks: WorkManager (for meal reminders)
- API: Gemini API (for calories & macros)
- Charts: Compose-compatible chart library

---

## 🏗️ Architecture Rules

- One ViewModel per screen
- Repository handles all data operations
- Room is the single source of truth
- UI observes StateFlow from ViewModel
- No business logic in UI layer

---

## 🧩 Core Features

### 1. User Onboarding
- Collect:
    - Name
    - Height (cm)
    - Weight (kg)
- Store in Room
- Show only on first launch

---

### 2. Meal Planning (WITH MULTIPLE OPTIONS)

Each meal can have **multiple selectable options**, and each option can have **multiple food items**.

#### Example:
Breakfast:
- Option 1: Paneer Sandwich (2 pieces)
- Option 2: Poha (1 bowl)
- Option 3: Moong Dal Dosa + Veggies

#### Rules:
- Each meal → multiple options
- Each option → multiple food items
- Each food item → quantity + macros

#### Gemini API Usage:
- Call API **once per food item during creation**
- Store:
    - calories
    - protein
    - carbs
    - fat
- Cache results in Room (DO NOT recompute later)

---

### 3. Notifications
- Notify user **10 minutes before meal time**
- Use WorkManager
- Each meal schedules one worker
- User can enable/disable reminders per meal

---

### 4. Meal Logging

- User selects **one option per meal per day**
- Options shown as **radio selection (single choice)**

#### Logging Rules:
- Only ONE option can be selected per meal per day
- Log stores selected option
- Macros calculated from selected option’s food items

---

### 5. Water Tracking
- User sets daily goal (ml)
- Quick-add buttons:
    - 250 ml
    - 500 ml
    - 750 ml
    - 1000 ml
- Each tap logs intake instantly

---

### 6. Progress Tracking
- Show:
    - Daily
    - Weekly
    - Monthly stats
- Metrics:
    - Calories
    - Protein, Carbs, Fat
    - Water intake
- Use charts:
    - Bar chart
    - Line chart

---

### 7. JSON Export / Import

#### Export:
- Export ALL data:
    - user profile
    - meals
    - meal options
    - food items
    - logs
    - water data
- Save as JSON file
- Share via Android Sharesheet

#### Import:
- Parse JSON
- Validate structure
- Replace existing data safely

---

## 📱 Screens

### 1. Home Screen
- Greeting with user name
- Live clock
- Today’s meals overview
- Water progress summary
- Floating Action Button → Export JSON

---

### 2. Water Intake Screen
- Show goal vs current intake
- Progress indicator
- Quick-add buttons

---

### 3. Meal Planner Screen
- List all meals
- Each meal contains:
    - multiple options
- UI:
    - Elevated Cards
    - Nested structure:
        - Meal → Options → Food Items
- Add/Edit using Modal Bottom Sheet

---

### 4. Meal Logging Screen
- Show today’s meals
- For each meal:
    - Show all options
    - Radio selection (single choice)
- Mark:
    - Completed
    - Skipped

---

### 5. Progress Screen
- Date range selector (Material Date Picker)
- Charts:
    - Calories
    - Water intake
- Filters:
    - Daily / Weekly / Monthly

---

## 🎨 UI/UX Guidelines

### Components (MANDATORY)
- NavigationBar
- TopAppBar
- FloatingActionButton
- ElevatedCard
- ModalBottomSheet
- DatePicker (modal)
- TimePicker (dial)

---

### Typography
- Primary: Roboto Flex
- Secondary: Serafina

---

### Theme
- Dark theme only
- Background: Black (#000000)
- Accent colors:
    - Light Green
    - Sky Blue
    - Light Purple

---

### Layout Rules
- Use:
    - Column
    - Row
    - LazyColumn
    - LazyVerticalGrid
- DO NOT use flexbox

---

## 🎬 Animations

- Water Goal Completion:
    - Fill animation → container fills → burst
- Meal Completion:
    - Scale + fade success animation

---

## ⚠️ Constraints

- No memory leaks
- Handle:
    - invalid input
    - empty states
    - API failure
- Offline support required
- All API responses cached
- Validate JSON before import

---

# 🧮 Data Models (Room Entities)

## UserProfileEntity
```kotlin
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val heightCm: Float,
    val weightKg: Float
)

MealEntity
@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val time: String, // HH:mm
    val reminderEnabled: Boolean
)

MealOptionEntity
@Entity(
    tableName = "meal_options",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("mealId")]
)
data class MealOptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val name: String
)

FoodItemEntity
@Entity(
    tableName = "food_items",
    foreignKeys = [
        ForeignKey(
            entity = MealOptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealOptionId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("mealOptionId")]
)
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealOptionId: Long,
    val name: String,
    val quantity: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)

MealLogEntity
@Entity(tableName = "meal_logs")
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val selectedOptionId: Long,
    val date: String, // YYYY-MM-DD
    val status: String, // COMPLETED / SKIPPED
    val timestamp: Long
)

WaterLogEntity
@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val date: String,
    val timestamp: Long
)

DailyWaterGoalEntity
@Entity(tableName = "water_goal")
data class DailyWaterGoalEntity(
    @PrimaryKey val id: Int = 1,
    val goalMl: Int
)


🔗 Relationships
Meal → MealOption (1:N)
MealOption → FoodItem (1:N)
Meal → MealLog (1:N)
WaterLog → standalone
🚀 Expected Outcome
Fully functional Android app
Clean architecture (MVVM)
Accurate meal tracking with selectable options
Reliable notifications
Offline-first behavior
Production-ready implementation