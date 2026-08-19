package com.dobedub.chotentuner

import com.dobedub.chotentuner.music.NoteReading
import kotlin.math.abs

enum class CharacterMood { IDLE, PERFECT, OFF, SING, SHOCK }

/**
 * All of the mascot's lines. Two personas: the loud angel streamer
 * 초텐짱 (light mode) and quiet 아메 (dark mode).
 */
object Dialogue {

    fun moodFor(
        micGranted: Boolean,
        mode: AppMode,
        reading: NoteReading?,
        tonePlaying: Boolean,
    ): CharacterMood = when {
        mode == AppMode.TUNER && !micGranted -> CharacterMood.SHOCK
        mode == AppMode.TUNER && reading == null -> CharacterMood.IDLE
        mode == AppMode.TUNER && abs(reading!!.cents) <= 5.0 -> CharacterMood.PERFECT
        mode == AppMode.TUNER -> CharacterMood.OFF
        tonePlaying -> CharacterMood.SING
        else -> CharacterMood.IDLE
    }

    /** Stable key describing the current situation; a line is drawn per bucket change. */
    fun bucketFor(
        micGranted: Boolean,
        mode: AppMode,
        reading: NoteReading?,
        tonePlaying: Boolean,
    ): String = when {
        mode == AppMode.TUNER && !micGranted -> "no_perm"
        mode == AppMode.TUNER && reading == null -> "tuner_idle"
        mode == AppMode.TUNER -> {
            val c = reading!!.cents
            when {
                abs(c) <= 5.0 -> "perfect"
                c <= -15 -> "low_big"
                c < 0 -> "low"
                c >= 15 -> "high_big"
                else -> "high"
            }
        }
        tonePlaying -> "tone_play"
        else -> "tone_idle"
    }

    fun linesFor(bucket: String, dark: Boolean): List<String> =
        (if (dark) ame[bucket] else choten[bucket]) ?: listOf("...")

    fun pokeLines(dark: Boolean): List<String> = if (dark) amePoke else chotenPoke

    fun transformLines(toDark: Boolean): List<String> = if (toDark) toDarkLines else toLightLines

    private val choten = mapOf(
        "no_perm" to listOf(
            "마이크 권한이 있어야 들을 수 있어! 부탁해 P~!",
            "권한 버튼 눌러줘! 지금 귀 막힌 상태야 ㅠㅠ",
        ),
        "tuner_idle" to listOf(
            "소리 들려줘~ 초텐짱이 듣고 있어! ♪",
            "P의 연주 기다리는 중~ 두근두근☆",
            "튜닝 타임! 자신있게 소리내봐~!",
        ),
        "low_big" to listOf(
            "삐용삐용! 많이 낮아~ 팍 감아올려!",
            "으엥, 한참 낮잖아! 힘내라 P!",
        ),
        "low" to listOf(
            "쪼~금 낮아! 살짝만 올려봐~",
            "아깝다! 요만~큼만 위로!",
        ),
        "high" to listOf(
            "쪼~금 높아! 살짝만 내려봐~",
            "아깝! 요만~큼만 아래로!",
        ),
        "high_big" to listOf(
            "꺄앗! 너무 높아!! 풀어줘 풀어줘~!",
            "높아높아! 살살 풀어보자~",
        ),
        "perfect" to listOf(
            "완벽해--☆ 역시 P는 천재야!",
            "딱 맞췄어! 천사 인증~☆",
            "그거야 그거!! 최고의 소리잖아?!",
        ),
        "tone_idle" to listOf(
            "듣고 싶은 음 눌러봐! 바로 내줄게~",
            "초텐짱 방송 준비 완료! 리퀘스트 받는다~☆",
        ),
        "tone_play" to listOf(
            "초텐짱 라이브 방송 중~ ♪♫",
            "이 음이야 이 음! 잘 들어봐~",
            "따라 불러도 돼! 같이 가보자고~",
        ),
    )

    private val ame = mapOf(
        "no_perm" to listOf(
            "...마이크 권한이 없으면 아무것도 안 들려.",
        ),
        "tuner_idle" to listOf(
            "...소리, 들려줘. 듣고 있으니까.",
            "연주해 줘. ...기다릴게.",
        ),
        "low_big" to listOf(
            "많이 낮아. ...더 감아.",
        ),
        "low" to listOf(
            "...조금 낮아. 올려봐.",
        ),
        "high" to listOf(
            "...조금 높아. 내려봐.",
        ),
        "high_big" to listOf(
            "너무 높아. ...풀어줘.",
        ),
        "perfect" to listOf(
            "...딱 맞아. 대단하네, P.",
            "완벽. ...조금 감동했어.",
        ),
        "tone_idle" to listOf(
            "...원하는 음, 눌러.",
        ),
        "tone_play" to listOf(
            "...흘러나오는 중. 잘 들어.",
        ),
    )

    private val chotenPoke = listOf(
        "꺄앗! 갑자기 만지면 부끄럽잖아~!",
        "승인욕구 충전 완료--☆ 고마워 P!",
        "P~ 나 귀엽지? 솔직해도 돼!",
        "머리 쓰다듬는 거야? 에헤헤~",
        "구독 좋아요 알림설정! ...아, 여긴 앱이었지?",
    )

    private val amePoke = listOf(
        "...뭐야, 갑자기.",
        "만지지 마. ...조금은 괜찮지만.",
        "...P는 이상해. (싫지 않아)",
        "지금은 이 모습이 편해.",
    )

    private val toDarkLines = listOf(
        "변신--... 아메 모드. ...잘 부탁해.",
        "...어두운 쪽이 진짜일지도.",
    )

    private val toLightLines = listOf(
        "변신--☆ 인터넷 엔젤 초텐짱 등장!",
        "짜잔! 천사 폼으로 컴백~☆",
    )
}
