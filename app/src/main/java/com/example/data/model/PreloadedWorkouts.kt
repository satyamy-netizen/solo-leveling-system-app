package com.example.data.model

data class PreloadedExercise(
    val name: String,
    val category: String, // "Chest & Triceps", "Back & Biceps", "Legs & Shoulders", "Abs & Cardio", "Full Body"
    val equipmentRequired: String, // "Full Gym", "Dumbbells Only", "Bodyweight"
    val baseSets: Int,
    val baseReps: String,
    val description: String,
    val xpReward: Int = 15
)

object PreloadedWorkouts {
    val workouts = listOf(
        // --- Chest & Triceps ---
        PreloadedExercise(
            name = "Barbell Bench Press",
            category = "Chest & Triceps",
            equipmentRequired = "Full Gym",
            baseSets = 4,
            baseReps = "8-12",
            description = "Lie on a flat bench, grip barbell slightly wider than shoulder width. Lower bar to mid-chest, then press upwards."
        ),
        PreloadedExercise(
            name = "Incline Dumbbell Press",
            category = "Chest & Triceps",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "10-12",
            description = "Sit on incline bench (30-45 deg), press dumbbells upwards from chest height until arms are straight."
        ),
        PreloadedExercise(
            name = "Cable Crossover",
            category = "Chest & Triceps",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "12-15",
            description = "Using high pulley cables, pull handles down and in a sweeping arch in front of your lower chest."
        ),
        PreloadedExercise(
            name = "Cable Rope Overhead Extension",
            category = "Chest & Triceps",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "12-15",
            description = "Using a double-rope attachment, face away from pulley, press rope overhead with triceps."
        ),
        PreloadedExercise(
            name = "Tricep Pushdown",
            category = "Chest & Triceps",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "10-12",
            description = "Hold cable attachment at upper chest level, extend elbow downwards to end range, locking out triceps."
        ),

        // Dumbbells Chest & Triceps
        PreloadedExercise(
            name = "Dumbbell Bench Press",
            category = "Chest & Triceps",
            equipmentRequired = "Dumbbells Only",
            baseSets = 4,
            baseReps = "10-12",
            description = "Lie flat on a bench, press dumbbells upwards to chest level, keeping shoulder blades retracted."
        ),
        PreloadedExercise(
            name = "Incline Dumbbell Flyes",
            category = "Chest & Triceps",
            equipmentRequired = "Dumbbells Only",
            baseSets = 3,
            baseReps = "12-15",
            description = "Lie on incline bench, lower dumbbells out in a wide sweeping arc, keeping a slight elbow bend, squeeze chest on the way up."
        ),
        PreloadedExercise(
            name = "Dumbbell Floor Press",
            category = "Chest & Triceps",
            equipmentRequired = "Dumbbells Only",
            baseSets = 4,
            baseReps = "8-12",
            description = "Lie on back on the floor, flat feet. Lower dumbbells until elbows rest on the floor, then push them up."
        ),
        PreloadedExercise(
            name = "Overhead Dumbbell Tricep Extension",
            category = "Chest & Triceps",
            equipmentRequired = "Dumbbells Only",
            baseSets = 3,
            baseReps = "10-12",
            description = "Hold a single heavy dumbbell vertical behind head on both hands, extend elbows upwards."
        ),
        PreloadedExercise(
            name = "Dumbbell Kickbacks",
            category = "Chest & Triceps",
            equipmentRequired = "Dumbbells Only",
            baseSets = 3,
            baseReps = "12-15",
            description = "Bend over slightly, keep arm locked beside torso. Extend only at elbow to kick the dumbbell backwards."
        ),

        // Bodyweight Chest & Triceps
        PreloadedExercise(
            name = "Standard Push-ups",
            category = "Chest & Triceps",
            equipmentRequired = "Bodyweight",
            baseSets = 4,
            baseReps = "15-20",
            description = "Core braced, flat body. Place hands slightly wider than shoulder width. Lower chest to floor and push up."
        ),
        PreloadedExercise(
            name = "Wide Chest Push-ups",
            category = "Chest & Triceps",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "12-15",
            description = "Take a wide hand placement to increase chest fiber stretch and load outer chest muscles."
        ),
        PreloadedExercise(
            name = "Decline Push-ups",
            category = "Chest & Triceps",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "10-12",
            description = "Elevate feet on a platform. Lower upper chest to target deep fibers of clavicular pectoral upper region."
        ),
        PreloadedExercise(
            name = "Diamond Push-ups",
            category = "Chest & Triceps",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "10-15",
            description = "Form a diamond shape with thumbs and index fingers under chest. Targets heavy triceps and inner chest line."
        ),
        PreloadedExercise(
            name = "Bench/Chair Dips",
            category = "Chest & Triceps",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "12-15",
            description = "Place hands on bench edge, extend feet forward. Bend arms to lower buttocks toward floor and lock out triceps."
        ),

        // --- Back & Biceps ---
        PreloadedExercise(
            name = "Barbell Deadlift",
            category = "Back & Biceps",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "5-8",
            description = "Lift weight from floor to hip height. Explode through heels, keeping spine strictly straight and neutral."
        ),
        PreloadedExercise(
            name = "Lat Pulldown",
            category = "Back & Biceps",
            equipmentRequired = "Full Gym",
            baseSets = 4,
            baseReps = "10-12",
            description = "Sit in machine, pull bar down to upper clavicle. Retract elbows downward to focus muscle squeeze in lats."
        ),
        PreloadedExercise(
            name = "Seated Cable Row",
            category = "Back & Biceps",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "10-12",
            description = "Pull cable towards mid-abdomen while stretching and retracting the shoulder blades."
        ),
        PreloadedExercise(
            name = "Pull-ups",
            category = "Back & Biceps",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "AMRAP (Max)",
            description = "Hang from bar, pull yourself upwards until chin surpasses bar, squeezing latissimus dorsi muscles."
        ),
        PreloadedExercise(
            name = "Barbell Bicep Curls",
            category = "Back & Biceps",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "8-12",
            description = "Hold barbell with underhand shoulder-width grip. Keeping elbows tucked, curl weight upwards to chin level."
        ),

        // Dumbbells Back & Biceps
        PreloadedExercise(
            name = "Dumbbell Bent-Over Row",
            category = "Back & Biceps",
            equipmentRequired = "Dumbbells Only",
            baseSets = 4,
            baseReps = "10-12",
            description = "Hinge hips back, draw dumbbells up to hips in row movement, keeping spine flat."
        ),
        PreloadedExercise(
            name = "One-Arm Dumbbell Row",
            category = "Back & Biceps",
            equipmentRequired = "Dumbbells Only",
            baseSets = 3,
            baseReps = "10-12",
            description = "Place knee and hand on flat bench. Row single dumbbell alongside ribs to hip."
        ),
        PreloadedExercise(
            name = "Dumbbell Pullover",
            category = "Back & Biceps",
            equipmentRequired = "Dumbbells Only",
            baseSets = 3,
            baseReps = "12-15",
            description = "Lie across flat bench, lower single dumbbell overhead with slightly bent elbows, stretching chest/lats."
        ),
        PreloadedExercise(
            name = "Alternating Dumbbell Curls",
            category = "Back & Biceps",
            equipmentRequired = "Dumbbells Only",
            baseSets = 3,
            baseReps = "10-12",
            description = "Stand straight. Pivot wrist upwards (supinate) while lifting dumbbells to contract biceps peak."
        ),
        PreloadedExercise(
            name = "Dumbbell Hammer Curls",
            category = "Back & Biceps",
            equipmentRequired = "Dumbbells Only",
            baseSets = 3,
            baseReps = "10-12",
            description = "Grip dumbbells vertically (neutral grip) and lift cleanly to work outer biceps head/brachialis."
        ),

        // Bodyweight Back & Biceps
        PreloadedExercise(
            name = "Overhand Pull-ups",
            category = "Back & Biceps",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "Max Reps",
            description = "Grip pull up bar overhand wider than shoulders. Pull chin over bar to build wide 'V-taper' lats."
        ),
        PreloadedExercise(
            name = "Underhand Chin-ups",
            category = "Back & Biceps",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "Max Reps",
            description = "Grip pull up bar underhand close width. Pull yourself up; shifts load focus onto biceps biceps curls."
        ),
        PreloadedExercise(
            name = "Inverted Bodyweight Row",
            category = "Back & Biceps",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "12-15",
            description = "Lie under low bar or table. Keeping heels on the floor and body stiff, pull upper chest up to the grip spot."
        ),
        PreloadedExercise(
            name = "Doorway Standing Rows",
            category = "Back & Biceps",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "15-20",
            description = "Stand in doorway, hold the frame edge on both fingertips, lean back, then row your weight forward."
        ),

        // --- Legs & Shoulders ---
        PreloadedExercise(
            name = "Barbell Back Squat",
            category = "Legs & Shoulders",
            equipmentRequired = "Full Gym",
            baseSets = 4,
            baseReps = "6-10",
            description = "Secure barbell on traps. Sunder bar, brace core, bend knees & push hips back to lower thighs parallel. Explode up!"
        ),
        PreloadedExercise(
            name = "Leg Press",
            category = "Legs & Shoulders",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "10-12",
            description = "Sit in sled, place feet shoulder-width on sled plate. Unlock bar and bend knees to 90 degrees, press up."
        ),
        PreloadedExercise(
            name = "Overhead Overhead Press",
            category = "Legs & Shoulders",
            equipmentRequired = "Full Gym",
            baseSets = 4,
            baseReps = "8-10",
            description = "Press barbell standing directly from shoulders straight overhead. Keeps core fully activated."
        ),
        PreloadedExercise(
            name = "Dumbbell Lateral Raise",
            category = "Legs & Shoulders",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "12-15",
            description = "Lift dumbbells horizontally with a slight elbow bend to build wide lateral shoulder deltoids size."
        ),

        // Dumbbell Legs & Shoulders
        PreloadedExercise(
            name = "Dumbbell Goblet Squat",
            category = "Legs & Shoulders",
            equipmentRequired = "Dumbbells Only",
            baseSets = 4,
            baseReps = "10-12",
            description = "Hold a massive dumbbell vertical under chin like a goblet. Lower into a squat and ascend cleanly."
        ),
        PreloadedExercise(
            name = "Dumbbell Romanian Deadlifts",
            category = "Legs & Shoulders",
            equipmentRequired = "Dumbbells Only",
            baseSets = 4,
            baseReps = "10-12",
            description = "Hold dumbbells in front of thighs, hinge hips back, and slide weights down shin bone, stretching hamstrings."
        ),
        PreloadedExercise(
            name = "Dumbbell Lunge",
            category = "Legs & Shoulders",
            equipmentRequired = "Dumbbells Only",
            baseSets = 3,
            baseReps = "10 steps/leg",
            description = "Holding dumbbells, take a giant stride forward, lower back knee to millimetres from floor, switch."
        ),
        PreloadedExercise(
            name = "Dumbbell Seated Press",
            category = "Legs & Shoulders",
            equipmentRequired = "Dumbbells Only",
            baseSets = 3,
            baseReps = "8-12",
            description = "Sit on high-back seat, press dumbbells directly overhead. Great for front/side delts safety."
        ),

        // Bodyweight Legs & Shoulders
        PreloadedExercise(
            name = "Bodyweight Squats",
            category = "Legs & Shoulders",
            equipmentRequired = "Bodyweight",
            baseSets = 4,
            baseReps = "20-25",
            description = "Squat fully down in deep full-range of motion, focusing heavily on quad and glute engagement."
        ),
        PreloadedExercise(
            name = "Walking Lunges",
            category = "Legs & Shoulders",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "15 steps/leg",
            description = "Continuous steps forward in structural lunges. Demands heavy stabilizers strength."
        ),
        PreloadedExercise(
            name = "Pike Push-ups",
            category = "Legs & Shoulders",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "8-12",
            description = "Form an inverted 'V' with hips high. Lower head towards ground between hands and push back. Front delts builder."
        ),
        PreloadedExercise(
            name = "Calf Raises",
            category = "Legs & Shoulders",
            equipmentRequired = "Bodyweight",
            baseSets = 4,
            baseReps = "25",
            description = "Stand on floor or step edge, elevate ankles up to top limit to trigger calf soleus contractions."
        ),

        // --- Abs & Cardio ---
        PreloadedExercise(
            name = "A-Rank Planks",
            category = "Abs & Cardio",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "60s hold",
            description = "Rest on forearms, brace whole abdominal grid, maintaining a fully straight core alignment."
        ),
        PreloadedExercise(
            name = "Harpin Ab Crunches",
            category = "Abs & Cardio",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "20-30",
            description = "Lie on back, pivot chest bone to hips, isolating core muscles cleanly."
        ),
        PreloadedExercise(
            name = "Mountain Climbers",
            category = "Abs & Cardio",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "45s speed",
            description = "Push up stance. Alternate driving knees explosively up to chest spot in rapid rhythmic pacing."
        ),
        PreloadedExercise(
            name = "Incline Barbell Bench Press",
            category = "Chest & Triceps",
            equipmentRequired = "Full Gym",
            baseSets = 4,
            baseReps = "8-12",
            description = "Similar to standard bench press but on a 30-45 degree incline bench, targeting superior chest fibers."
        ),
        PreloadedExercise(
            name = "Decline Barbell Bench Press",
            category = "Chest & Triceps",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "8-12",
            description = "Similar to flat bench press but on a decline bench, prioritizing lower chest engagement."
        ),
        PreloadedExercise(
            name = "Dumbbell Shrugs",
            category = "Back & Biceps",
            equipmentRequired = "Dumbbells Only",
            baseSets = 3,
            baseReps = "12-15",
            description = "Hold heavy dumbbells by your sides. Pull shoulders up toward ears to build strong upper trapezius muscles."
        ),
        PreloadedExercise(
            name = "Barbell Row (Yates Row)",
            category = "Back & Biceps",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "8-12",
            description = "Stand with a barbell underhand grip, hinge forward slightly, row barbell toward lower waist."
        ),
        PreloadedExercise(
            name = "Face Pulls",
            category = "Legs & Shoulders",
            equipmentRequired = "Full Gym",
            baseSets = 4,
            baseReps = "15-20",
            description = "Using high pulley rope attachment, pull rope towards your forehead/nose, splitting the rope to build rear delts."
        ),
        PreloadedExercise(
            name = "Arnold Press",
            category = "Legs & Shoulders",
            equipmentRequired = "Dumbbells Only",
            baseSets = 3,
            baseReps = "8-12",
            description = "Seated, hold dumbbells with palms facing you. Rotate hands as you press dumbbells up so palms face outward at the top."
        ),
        PreloadedExercise(
            name = "Bulgarian Split Squat",
            category = "Legs & Shoulders",
            equipmentRequired = "Dumbbells Only",
            baseSets = 3,
            baseReps = "8-12 each",
            description = "Place one foot back on a flat bench. Lower hips with single front leg to construct extreme strength and balance."
        ),
        PreloadedExercise(
            name = "Leg Extensions",
            category = "Legs & Shoulders",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "12-15",
            description = "Sit in machine, lock shins behind rollers. Extend knees upward to isolate front quad muscles."
        ),
        PreloadedExercise(
            name = "Seated Leg Curl",
            category = "Legs & Shoulders",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "12-15",
            description = "Sit in machine, flex knees downwards to contract and develop posterior hamstring fibers."
        ),
        PreloadedExercise(
            name = "Preacher Bicep Curl",
            category = "Back & Biceps",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "10-12",
            description = "Rest arms against angled preacher bench pad. Hold EZ bar, curl upward fully to isolate biceps peak contraction."
        ),
        PreloadedExercise(
            name = "Skull Crushers",
            category = "Chest & Triceps",
            equipmentRequired = "Full Gym",
            baseSets = 3,
            baseReps = "10-12",
            description = "Lie on bench holding EZ bar/dumbbells. Move arms slight angle, bend elbows to lower bar to forehead, extend up."
        ),
        PreloadedExercise(
            name = "Hanging Leg Raises",
            category = "Abs & Cardio",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "10-15",
            description = "Hang straight from pull-up bar. Keeping legs fully straight, raise them to horizontal level using core."
        ),
        PreloadedExercise(
            name = "Russian Twists",
            category = "Abs & Cardio",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "20-30",
            description = "Sit with knees bent, feet slightly off the floor. Twist torso side to side, tapping floor with hands."
        ),
        PreloadedExercise(
            name = "Burpees",
            category = "Abs & Cardio",
            equipmentRequired = "Bodyweight",
            baseSets = 3,
            baseReps = "12-15",
            description = "Standard dynamic full body cardio mover. Perform a push-up, jump feet in, then explosively jump vertically."
        )
    )

    fun getSuggestedWorkouts(category: String, equipment: String): List<PreloadedExercise> {
        return workouts.filter {
            it.category == category &&
            (it.equipmentRequired == equipment || it.equipmentRequired == "Bodyweight")
        }
    }
}
