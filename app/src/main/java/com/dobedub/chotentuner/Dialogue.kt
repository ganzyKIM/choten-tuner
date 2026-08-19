package com.dobedub.chotentuner

import com.dobedub.chotentuner.music.NoteReading
import kotlin.math.abs

/**
 * Which pose the mascot strikes. Each persona maps these to its own artwork
 * (see CharacterZone), so the situation drives the expression for both forms.
 */
enum class Sprite { NEUTRAL, JOY, SHARP, FLAT, SHY, DARK }

/**
 * All of the mascot's lines and poses. Two personas: the loud angel streamer
 * 초텐짱 (light form) and quiet 아메 (dark form).
 */
object Dialogue {

    /**
     * Tone colour of a single pitch, by its interval above C:
     * perfect intervals read as plain, major ones bright, minor ones and the
     * tritone dark. Drives both the pose and the line in 소리내기 mode.
     */
    fun toneQuality(midi: Int): String = when (((midi % 12) + 12) % 12) {
        0, 5, 7 -> "plain"       // C, F, G — 완전음정
        2, 4, 9, 11 -> "bright"  // D, E, A, B — 장음정
        else -> "dark"           // C#, D#, F#, G#, A# — 단음정 + 트라이톤
    }

    /** Stable key describing the current situation; pose and line follow from it. */
    fun bucketFor(
        micGranted: Boolean,
        mode: AppMode,
        reading: NoteReading?,
        tonePlaying: Boolean,
        toneMidi: Int,
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
        tonePlaying -> "tone_" + toneQuality(toneMidi)
        else -> "tone_idle"
    }

    fun spriteFor(bucket: String): Sprite = when (bucket) {
        "perfect", "tone_bright" -> Sprite.JOY
        "low", "low_big" -> Sprite.FLAT
        "high", "high_big", "no_perm" -> Sprite.SHARP
        "tone_dark" -> Sprite.DARK
        else -> Sprite.NEUTRAL
    }

    fun linesFor(bucket: String, dark: Boolean): List<String> =
        (if (dark) ame[bucket] else choten[bucket]) ?: listOf("...")

    fun pokeLines(dark: Boolean): List<String> = if (dark) amePoke else chotenPoke

    fun transformLines(toDark: Boolean): List<String> = if (toDark) toDarkLines else toLightLines

    private val choten = mapOf(
        "no_perm" to listOf(
            "마이크 권한이 있어야 들을 수 있어! 부탁해 P~!",
            "권한 버튼 눌러줘! 지금 귀 막힌 상태야 ㅠㅠ",
            "안 들려안 들려! 이래선 방송 못 한다구!",
        ),
        "tuner_idle" to listOf(
            "소리 들려줘~ 초텐짱이 듣고 있어! ♪",
            "P의 연주 기다리는 중~ 두근두근☆",
            "튜닝 타임! 자신있게 소리내봐~!",
            "귀 쫑긋 세우고 대기중이야! 언제든 오케이~",
        ),
        "low_big" to listOf(
            "삐용삐용! 많이 낮아~ 팍 감아올려!",
            "으엥, 한참 낮잖아! 힘내라 P!",
            "너무 낮아!! 지하실까지 내려갔어~!",
        ),
        "low" to listOf(
            "쪼~금 낮아! 살짝만 올려봐~",
            "아깝다! 요만~큼만 위로!",
            "거의 다 왔어! 아주 살짝 올려~",
        ),
        "high" to listOf(
            "쪼~금 높아! 살짝만 내려봐~",
            "아깝! 요만~큼만 아래로!",
            "다 왔는데! 아주 조금만 내려봐~",
        ),
        "high_big" to listOf(
            "꺄앗! 너무 높아!! 풀어줘 풀어줘~!",
            "높아높아! 살살 풀어보자~",
            "우주까지 날아갔어! 진정해 P!",
        ),
        "perfect" to listOf(
            "완벽해--☆ 역시 P는 천재야!",
            "딱 맞췄어! 천사 인증~☆",
            "그거야 그거!! 최고의 소리잖아?!",
            "짜자잔-! 완벽한 음정 완성~!",
            "우와아! 소름돋았어! 이게 프로구나~☆",
        ),
        "tone_idle" to listOf(
            "듣고 싶은 음 눌러봐! 바로 내줄게~",
            "초텐짱 방송 준비 완료! 리퀘스트 받는다~☆",
            "어떤 음이 좋아? 뭐든 말만 해!",
        ),
        "tone_bright" to listOf(
            "우와~ 밝고 예쁜 음이다! 기분 좋아져~☆",
            "이거 완전 상큼한 소리! 텐션 올라간다구~!",
            "반짝반짝한 음이야! 초텐짱이랑 딱이네~♪",
        ),
        "tone_dark" to listOf(
            "오~ 좀 어른스러운 음이네~ 시크해...",
            "쓸쓸한 소리다... 이런 것도 나쁘지 않지?",
            "묘하게 아련한 음이야... 감성 폭발~",
        ),
        "tone_plain" to listOf(
            "탄탄한 기준음이야! 여기서부터 시작~♪",
            "딱 중심 잡아주는 음! 든든하지~?",
            "기본에 충실한 소리! 튜닝의 뼈대라구~☆",
        ),
    )

    private val ame = mapOf(
        "no_perm" to listOf(
            "...마이크 권한이 없으면 아무것도 안 들려.",
            "귀를 막아둔 채로 뭘 하라는 거야.",
        ),
        "tuner_idle" to listOf(
            "...소리, 들려줘. 듣고 있으니까.",
            "연주해 줘. ...기다릴게.",
            "...조용하네. 나쁘지 않지만.",
        ),
        "low_big" to listOf(
            "많이 낮아. ...더 감아.",
            "한참 낮아. ...제대로 좀 해.",
        ),
        "low" to listOf(
            "...조금 낮아. 올려봐.",
            "아깝네. ...조금만 더.",
        ),
        "high" to listOf(
            "...조금 높아. 내려봐.",
            "거의 다 왔어. ...살짝만.",
        ),
        "high_big" to listOf(
            "너무 높아. ...풀어줘.",
            "그렇게 조이면... 끊어져 버려.",
        ),
        "perfect" to listOf(
            "...딱 맞아. 대단하네, P.",
            "완벽. ...조금 감동했어.",
            "...이런 소리, 계속 듣고 싶어.",
        ),
        "tone_idle" to listOf(
            "...원하는 음, 눌러.",
            "뭐든 내줄게. ...말만 해.",
        ),
        "tone_bright" to listOf(
            "...밝은 음이네. 눈부셔.",
            "이런 소리는... 조금 부끄러워.",
        ),
        "tone_dark" to listOf(
            "...이 음, 좋아해. 어둡고 깊어서.",
            "가라앉는 소리... 머리 속이 조용해져.",
            "...계속 울리게 둘게.",
        ),
        "tone_plain" to listOf(
            "...흔들림 없는 음이야.",
            "기준이 되는 소리. ...여기 기대도 돼.",
        ),
    )

    private val chotenPoke = listOf(
        "꺄앗! 갑자기 만지면 부끄럽잖아~!",
        "승인욕구 충전 완료--☆ 고마워 P!",
        "P~ 나 귀엽지? 솔직해도 돼!",
        "머리 쓰다듬는 거야? 에헤헤~",
        "구독 좋아요 알림설정! ...아, 여긴 앱이었지?",
        "에헤헤~ 더 해줘도 되는데~?",
    )

    private val amePoke = listOf(
        "...뭐야, 갑자기.",
        "만지지 마. ...조금은 괜찮지만.",
        "...P는 이상해. (싫지 않아)",
        "지금은 이 모습이 편해.",
        "...따뜻하네. 조금만 더 있어줘.",
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
