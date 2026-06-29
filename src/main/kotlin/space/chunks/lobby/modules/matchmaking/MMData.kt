package space.chunks.lobby.modules.matchmaking

import chunks.space.api.explorer.chunk.v1alpha1.Types
import chunks.space.api.matchmaking.v1alpha1.Api

class MMData(
    val ticket: Api.Ticket,
    val chunk: Types.Chunk,
    val flavor: Types.Flavor,
    val actorId: String,
)