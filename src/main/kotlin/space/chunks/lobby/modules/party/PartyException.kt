package space.chunks.lobby.modules.party

enum class PartyExceptionReason{
    PARTY_GONE,
    INVITE_GONE,
    NOT_OWNER,
    INVITER_IS_INVITEE,
    INVITE_ALREADY_PENDING,
    PLAYER_ALREADY_IN_PARTY,
}

class PartyException(val reason: PartyExceptionReason) : RuntimeException()
