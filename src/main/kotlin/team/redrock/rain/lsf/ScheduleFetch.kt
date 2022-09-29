package team.redrock.rain.lsf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * 拉取课表数据
 *
 * @param stuId 学号
 * @param theWeek 指定周,默认为0,即全部周
 * @return
 */
suspend fun fetchLessonSchedule(stuId: String, theWeek: Int = 0): StudentLessonData {
    val lessons = mutableListOf<LessonWeekData>()
    val document = withContext(Dispatchers.IO) {
        Jsoup.connect("http://jwzx.cqupt.edu.cn/kebiao/kb_stu.php?xh=${stuId}").get()
    }
    // 忽略第一个
    val theadTds = document.select("#stuPanel thead tr td")
        .next()
    theadTds.forEach { td ->
        lessons.add(
            LessonWeekData(
                label = td.text(),
                data = mutableListOf()
            )
        )
    }
    val tbodys = document.select("#stuPanel table tbody")
    tbodys.forEach { tbody ->
        tbody.select("tr").forEach { tr ->
            val label = tr.select("td")
            label.next()
                .map { it.select(".kbTd") }
                .forEachIndexed { i, kbTds ->
                    lessons[i].data.add(
                        LessonDayData(
                            label = label.first()!!.text(),
                            data = mutableListOf()
                        )
                    )
                    kbTds.forEach each@ { kbTd ->
                        val weekFlags = kbTd.attr("zc").toCharArray()
                        if (theWeek != 0 && weekFlags[i] == '0') {
                            return@each
                        }
                        val texts = kbTd.html().split("<br>")
                            .map {
                                it.trim()
                                    .replace("\n", "")
                                    .replace("\t", "")
                                    .replace("<font color=\"#FF0000\">", "")
                                    .replace("</font>", "")
                                    .replace("<span style=\"color:#0000FF\">", "")
                                    .replace("</span>", "")
                            }
                        var (
                            lessonId,
                            lessonName,
                            classroom,
                            weeks,
                            teacherTypeCredit,
                        ) = texts
                        val stuListId = Jsoup.parse(texts[5]).select("a").attr("href").split("jxb=")[1]
                        classroom = classroom.replace("地点: ", "")
                        val (teacher, type, credit) = teacherTypeCredit.split(" ")
                        lessons[i].data.last().data.add(
                            SingleLessonData(
                                lessonId,
                                lessonName,
                                classroom,
                                weeks,
                                teacher,
                                type,
                                credit.replace("学分", "").toDouble(),
                                stuListId
                            )
                        )
                    }
                }
        }
    }
    return StudentLessonData(
        stuId = stuId,
        lessons = lessons
    )
}

suspend fun main() {
    println(fetchLessonSchedule("2021214414"))
}