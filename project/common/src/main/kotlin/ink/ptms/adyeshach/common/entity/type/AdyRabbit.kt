package ink.ptms.adyeshach.common.entity.type

import org.bukkit.entity.Rabbit

/**
 * @author sky
 * @date 2020/8/4 23:15
 */
abstract class AdyRabbit : AdyEntityAgeable() {

    open fun setType(value: Rabbit.Type) {
        setMetadata("type", value.ordinal)
    }

    open fun getType(): Rabbit.Type {
        return Rabbit.Type.values()[getMetadata("type")]
    }
}