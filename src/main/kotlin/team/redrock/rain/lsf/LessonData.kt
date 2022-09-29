package team.redrock.rain.lsf

data class StudentLessonData(
    val stuId: String,
    val lessons: List<LessonWeekData>
)

data class LessonWeekData(
    val label: String,
    val data: MutableList<LessonDayData>
)

data class LessonDayData(
    val label: String,
    val data: MutableList<SingleLessonData>
)

data class SingleLessonData(
    val lessonId: String,
    val lessonName: String,
    val classroom: String,
    val weeks: String,
    val teacher: String,
    val type: String,
    val credit: Double,
    val stuListId: String
)