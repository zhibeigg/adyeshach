package ink.ptms.adyeshach.impl.nms

import com.github.benmanes.caffeine.cache.Caffeine
import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.AdyeshachEntityTypeRegistry
import ink.ptms.adyeshach.core.MinecraftHelper
import ink.ptms.adyeshach.core.bukkit.BukkitPaintings
import ink.ptms.adyeshach.core.bukkit.BukkitParticles
import ink.ptms.adyeshach.core.entity.EntityTypes
import ink.ptms.adyeshach.core.util.errorBy
import ink.ptms.adyeshach.impl.nmspaper.NMSPaper
import ink.ptms.adyeshach.minecraft.ChunkPos
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.TropicalFish
import org.bukkit.material.MaterialData
import org.bukkit.util.Vector
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.nms.MinecraftVersion

/**
 * Adyeshach
 * ink.ptms.adyeshach.impl.nms.DefaultMinecraftUtils
 *
 * @author 坏黑
 * @since 2022/6/28 00:07
 */
class DefaultMinecraftHelper : MinecraftHelper {

    val majorLegacy = MinecraftVersion.majorLegacy

    val typeHandler: AdyeshachEntityTypeRegistry
        get() = Adyeshach.api().getEntityTypeRegistry()

    val nms13ParticleRegistryBlocks: NMS13IRegistry<NMS13Particle<out NMS13ParticleParam>>
        get() = NMS13IRegistry::class.java.getProperty("PARTICLE_TYPE", isStatic = true)!!

    val entityTypeCache = Caffeine.newBuilder()
        .expireAfterAccess(30, java.util.concurrent.TimeUnit.MINUTES)
        .build<EntityTypes, Any>()

    val paintingCache = Caffeine.newBuilder()
        .expireAfterAccess(30, java.util.concurrent.TimeUnit.MINUTES)
        .build<BukkitPaintings, Any>()

    val particleCache = Caffeine.newBuilder()
        .expireAfterAccess(30, java.util.concurrent.TimeUnit.MINUTES)
        .build<BukkitParticles, Any>()

    val blockIdCache = Caffeine.newBuilder()
        .expireAfterAccess(30, java.util.concurrent.TimeUnit.MINUTES)
        .build<MaterialData, Int>()

    override fun adapt(type: EntityTypes): Any {
        return entityTypeCache.get(type) {
            if (majorLegacy >= 11400) {
                val names = ArrayList<String>()
                names += type.name
                names += typeHandler.getBukkitEntityAliases(type)
                names.forEach { kotlin.runCatching { return@get NMS16EntityTypes::class.java.getProperty<Any>(it, isStatic = true)!! } }
                errorBy("error-entity-type-not-supported", "$type $names")
            } else {
                typeHandler.getBukkitEntityId(type)
            }
        }!!
    }

    override fun adapt(location: Location): Any {
        return NMSBlockPosition(location.blockX, location.blockY, location.blockZ)
    }

    override fun adapt(paintings: BukkitPaintings): Any {
        return paintingCache.get(paintings) {
            if (MinecraftVersion.major >= 5) {
                NMS16Paintings::class.java.getProperty<Any>(paintings.index.toString(), isStatic = true)!!
            } else {
                paintings.legacy!!
            }
        }!!
    }

    @Suppress("KotlinConstantConditions")
    override fun adapt(particles: BukkitParticles): Any {
        return particleCache.get(particles) {
            when {
                majorLegacy >= 11400 -> {
                    NMS16Particles::class.java.getProperty<Any>(particles.name, isStatic = true) ?: NMS16Particles.FLAME
                }

                majorLegacy >= 11300 -> {
                    val particle = nms13ParticleRegistryBlocks.get(NMS13MinecraftKey(particles.name.lowercase()))
                    if (particle is NMS13Particle<*>) {
                        particle.f()
                    } else {
                        particle
                    }
                }

                else -> 0
            }
        }!!
    }

    override fun adaptTropicalFishPattern(data: Int): TropicalFish.Pattern {
        return CraftTropicalFishPattern19.fromData(data and '\uffff'.code)
    }

    override fun adaptTropicalFishPattern(pattern: TropicalFish.Pattern): Int {
        return CraftTropicalFishPattern19.values()[pattern.ordinal].dataValue
    }

    override fun getEntity(world: World, id: Int): Entity? {
        return (world as CraftWorld16).handle.getEntity(id)?.bukkitEntity
    }

    override fun getEntityDataWatcher(entity: Entity): Any {
        // 1.19 dataWatcher -> entityData
        return if (majorLegacy >= 11900) {
            (entity as CraftEntity19).handle.entityData
        } else {
            (entity as CraftEntity16).handle.dataWatcher
        }
    }

    override fun getBlockId(materialData: MaterialData): Int {
        return blockIdCache.get(materialData) {
            if (MinecraftVersion.major >= 10) {
                NMSBlock.getId(CraftMagicNumbers19.getBlock(materialData))
            } else if (MinecraftVersion.major >= 5) {
                NMS16Block.getCombinedId(CraftMagicNumbers16.getBlock(materialData))
            } else {
                materialData.itemType.id + (materialData.data.toInt() shl 12)
            }
        }!!
    }

    override fun vec3dToVector(vec3d: Any): Vector {
        return Vector((vec3d as NMS16Vec3D).x, vec3d.y, vec3d.z)
    }

    override fun craftChatSerializerToJson(compound: Any): String {
        return if (MinecraftVersion.isUniversal) {
            NMSChatSerializer.toJson(compound as NMSIChatBaseComponent)
        } else {
            NMS16ChatSerializer.a(compound as NMS16IChatBaseComponent)
        }
    }

    override fun craftChatMessageFromString(message: String): Any {
        return CraftChatMessage19.fromString(message)[0]
    }

    override fun isChunkVisible(player: Player, chunkX: Int, chunkZ: Int): Boolean {
        // 你改你妈个蛋，我爱说实话
        return try {
            NMSPaper.instance.isChunkSent(player, chunkX, chunkZ)
        } catch (ex: Throwable) {
            // 从 1.18 开始 getVisibleChunk  -> getVisibleChunkIfPresent
            //             getChunkProvider -> getChunkSource
            if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_18)) {
                val craftWorld = player.world as CraftWorld19
                craftWorld.handle.chunkSource.chunkMap.visibleChunkMap.get(ChunkPos.asLong(chunkX, chunkZ)) != null
            }
            // 从 1.14 开始，PlayerChunkMap 改版
            else if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_14)) {
                val craftWorld = player.world as CraftWorld16
                craftWorld.handle.chunkProvider.playerChunkMap.getVisibleChunk(ChunkPos.asLong(chunkX, chunkZ)) != null
            }
            // 早期版本
            else {
                val craftWorld = player.world as CraftWorld12
                craftWorld.handle.playerChunkMap.getChunk(chunkX, chunkZ) != null
            }
        }
    }
}