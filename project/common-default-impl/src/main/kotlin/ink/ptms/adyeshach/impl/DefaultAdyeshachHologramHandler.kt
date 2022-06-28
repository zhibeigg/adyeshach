package ink.ptms.adyeshach.impl

import ink.ptms.adyeshach.common.api.AdyeshachHologram
import ink.ptms.adyeshach.common.api.AdyeshachHologramHandler
import ink.ptms.adyeshach.common.entity.EntityTypes
import ink.ptms.adyeshach.common.entity.type.AdyEntity
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer
import java.util.function.Function

/**
 * Adyeshach
 * ink.ptms.adyeshach.impl.DefaultAdyeshachHologramHandler
 *
 * @author 坏黑
 * @since 2022/6/28 00:05
 */
class DefaultAdyeshachHologramHandler : AdyeshachHologramHandler {

    override fun createHologramItem(text: String): AdyeshachHologram.ItemByText {
        TODO("Not yet implemented")
    }

    override fun createHologramItem(text: ItemStack): AdyeshachHologram.ItemByItemStack {
        TODO("Not yet implemented")
    }

    override fun <T : AdyEntity> createHologramItem(type: EntityTypes, text: Consumer<T>): AdyeshachHologram.ItemByEntity<T> {
        TODO("Not yet implemented")
    }

    override fun createHologram(player: Player, location: Location, content: List<AdyeshachHologram.Item>): AdyeshachHologram {
        TODO("Not yet implemented")
    }

    override fun createHologramByText(player: Player, location: Location, content: List<String>): AdyeshachHologram {
        TODO("Not yet implemented")
    }

    override fun createHologramMessage(player: Player, location: Location, message: List<String>, stay: Long, transfer: Function<String, String>) {
        TODO("Not yet implemented")
    }
}