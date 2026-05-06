package space.chunks.lobby.ui

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import space.chunks.lobby.pack.Items

class Hotbar {
    companion object {
        fun give(player: Player) {

            val mm = MiniMessage.miniMessage()

            val teleporter = ItemStack(Material.PAPER)
            teleporter.editMeta {
                it.itemModel = Items.TELEPORTER
                it.displayName(mm.deserialize("Teleporter"))
            }
            val map = ItemStack(Material.PAPER)
            map.editMeta {
                it.itemModel = Items.MAP
                it.displayName(mm.deserialize("Warps"))
            }
            val inventory = ItemStack(Material.PAPER)
            inventory.editMeta {
                it.itemModel = Items.INVENTORY
                it.displayName(mm.deserialize("Inventory"))
            }
            val party = ItemStack(Material.PAPER)
            party.editMeta {
                it.itemModel = Items.PARTY
                it.displayName(mm.deserialize("Party"))
            }
            val settings = ItemStack(Material.PAPER)
            settings.editMeta {
                it.itemModel = Items.SETTINGS
                it.displayName(mm.deserialize("Settings"))
            }

            player.inventory.setItem(0, teleporter)
            player.inventory.setItem(1, map)
            player.inventory.setItem(2, inventory)
            player.inventory.setItem(3, party)
            player.inventory.setItem(4, settings)

        }
    }

}