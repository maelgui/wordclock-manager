package fr.maelgui.wordclockmanager

class WordclockConst {
    enum class Mode {
        ERROR,
        OFF,
        ON,
        TIME,
        AMBIENT;

        companion object {
            private val VALUES = Mode.values();
            fun fromInt(value: Int) = VALUES.first { it.ordinal == value }
        }
    }

    enum class Function {
        ERROR,
        HOUR,
        TEMPERATURE,
        TIMER,
        ALTERNATE;

        companion object {
            private val VALUES = Function.values();
            fun fromInt(value: Int) = VALUES.first { it.ordinal == value }
        }
    }

    enum class Rotation {
        ROT_0,
        ROT_90,
        ROT_180,
        ROT_270;

        companion object {
            private val VALUES = Rotation.values();
            fun fromInt(value: Int) = VALUES.first { it.ordinal == value }
        }
    }
}