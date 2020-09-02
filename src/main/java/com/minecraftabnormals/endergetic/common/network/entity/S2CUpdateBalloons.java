package com.minecraftabnormals.endergetic.common.network.entity;

import com.minecraftabnormals.endergetic.common.entities.bolloom.BolloomBalloonEntity;
import com.minecraftabnormals.endergetic.core.EndergeticExpansion;
import com.minecraftabnormals.endergetic.core.interfaces.BalloonHolder;
import com.teamabnormals.abnormals_core.client.ClientInfo;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public final class S2CUpdateBalloons {
	private int entityId;
	private int[] balloonIds;

	private S2CUpdateBalloons(int entityId, int[] balloonIds) {
		this.entityId = entityId;
		this.balloonIds = balloonIds;
	}

	public S2CUpdateBalloons(Entity entity) {
		this.entityId = entity.getEntityId();
		List<BolloomBalloonEntity> balloons = ((BalloonHolder) entity).getBalloons();
		this.balloonIds = new int[balloons.size()];
		for (int i = 0; i < balloons.size(); ++i) {
			BolloomBalloonEntity balloon = balloons.get(i);
			this.balloonIds[i] = balloon == null ? -1 : balloon.getEntityId();
		}
	}

	public void serialize(PacketBuffer buf) {
		buf.writeVarInt(this.entityId);
		buf.writeVarIntArray(this.balloonIds);
	}

	public static S2CUpdateBalloons deserialize(PacketBuffer buf) {
		return new S2CUpdateBalloons(buf.readVarInt(), buf.readVarIntArray());
	}

	public static void handle(S2CUpdateBalloons message, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
			context.enqueueWork(() -> {
				World world = ClientInfo.getClientPlayerWorld();
				Entity entity = world.getEntityByID(message.entityId);
				if (entity == null) {
					EndergeticExpansion.LOGGER.warn("Received balloons for unknown entity!");
				} else {
					BalloonHolder balloonHolder = (BalloonHolder) entity;
					balloonHolder.unattachBalloons();

					for (int i = 0; i < message.balloonIds.length; i++) {
						int id = message.balloonIds[i];
						if (id == -1) {
							balloonHolder.getBalloons().set(i, null);
						} else {
							Entity balloon = world.getEntityByID(id);
							if (balloon instanceof BolloomBalloonEntity) {
								balloonHolder.attachBalloon((BolloomBalloonEntity) balloon);
							}
						}
					}
				}
			});
		}
		context.setPacketHandled(true);
	}
}