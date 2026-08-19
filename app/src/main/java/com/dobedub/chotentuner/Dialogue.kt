package com.dobedub.chotentuner

import com.dobedub.chotentuner.music.NoteReading

/**
 * Which pose the mascot strikes. Each persona maps these to its own artwork
 * (see CharacterZone), so the situation drives the expression for both forms.
 */
enum class Sprite { NEUTRAL, JOY, SHARP, FLAT, SHY, DARK }

/** A pose paired with the line that goes with it. */
data class Reaction(val sprite: Sprite, val line: String)

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

    /**
     * Stable key describing the current situation; pose and line follow from it.
     *
     * While the tuner actually hears something the key stays "tuner_on" no matter
     * how the pitch moves — the meter reports the deviation, and the mascot
     * holding one pose keeps her out of the way.
     */
    fun bucketFor(
        micGranted: Boolean,
        mode: AppMode,
        reading: NoteReading?,
        tonePlaying: Boolean,
        toneMidi: Int,
    ): String = when {
        mode == AppMode.TUNER && !micGranted -> "no_perm"
        mode == AppMode.TUNER && reading == null -> "tuner_idle"
        mode == AppMode.TUNER -> "tuner_on"
        tonePlaying -> "tone_" + toneQuality(toneMidi)
        else -> "tone_idle"
    }

    fun spriteFor(bucket: String): Sprite = when (bucket) {
        "no_perm" -> Sprite.SHARP
        "tuner_idle" -> Sprite.FLAT
        "tone_bright" -> Sprite.JOY
        "tone_dark" -> Sprite.DARK
        else -> Sprite.NEUTRAL
    }

    fun linesFor(bucket: String, dark: Boolean): List<String> =
        (if (dark) ame[bucket] else choten[bucket]) ?: listOf("...")

    fun pokeReactions(dark: Boolean): List<Reaction> = if (dark) amePoke else chotenPoke

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
        "tuner_on" to listOf(
            "듣고 있어~ 천천히 맞춰봐!",
            "오케이 오케이! 미터기 보면서 조율해~☆",
            "초텐짱이 집중해서 듣는 중~ ♪",
            "소리 잡았다! 이제 P의 손끝에 달렸어~",
            "조용히 있어줄게. 편하게 맞춰봐~",
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
        "tuner_on" to listOf(
            "...듣고 있어. 천천히 해.",
            "소리, 잡았어. ...서두르지 마.",
            "...방해 안 할게. 계속해.",
            "괜찮아. ...끝까지 봐줄 테니까.",
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

    /** Poking her cycles through every pose she has, each with its own line. */
    private val chotenPoke = listOf(
        Reaction(Sprite.SHY, "꺄앗! 갑자기 만지면 부끄럽잖아~!"),
        Reaction(Sprite.SHY, "머리 쓰다듬는 거야? 에헤헤~"),
        Reaction(Sprite.SHY, "에헤헤~ 더 해줘도 되는데~?"),
        Reaction(Sprite.JOY, "승인욕구 충전 완료--☆ 고마워 P!"),
        Reaction(Sprite.JOY, "P~ 나 귀엽지? 솔직해도 돼!"),
        Reaction(Sprite.JOY, "브이~☆ 사진이라도 찍어줄 거야?"),
        Reaction(Sprite.SHARP, "히익! 놀랐잖아~! 심장 떨어질 뻔했어!"),
        Reaction(Sprite.SHARP, "야야야! 머리 헝클어진다구~!"),
        Reaction(Sprite.FLAT, "음~? 왜 불렀어? 나 쉬는 중인데~"),
        Reaction(Sprite.FLAT, "지금은 힐링 타임이라구~ 조금만 기다려~"),
        Reaction(Sprite.NEUTRAL, "응? 무슨 일이야 P?"),
        Reaction(Sprite.NEUTRAL, "부르셨습니까~ 초텐짱 대기중!"),
    )

    private val amePoke = listOf(
        Reaction(Sprite.SHY, "...뭐야, 갑자기."),
        Reaction(Sprite.SHY, "만지지 마. ...조금은 괜찮지만."),
        Reaction(Sprite.SHY, "...따뜻하네. 조금만 더 있어줘."),
        Reaction(Sprite.DARK, "...한 알 줄까? 농담이야."),
        Reaction(Sprite.DARK, "머리가 몽롱해... 네 탓이야."),
        Reaction(Sprite.FLAT, "...연기 마시지 마. 몸에 나빠."),
        Reaction(Sprite.FLAT, "쉬는 중이야. ...옆에 있어도 돼."),
        Reaction(Sprite.SHARP, "...계속 만지면, 못 놓아줄지도."),
        Reaction(Sprite.SHARP, "도망갈 생각은... 하지 마."),
        Reaction(Sprite.NEUTRAL, "...왜."),
        Reaction(Sprite.NEUTRAL, "...보고 있었어. 계속."),
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
