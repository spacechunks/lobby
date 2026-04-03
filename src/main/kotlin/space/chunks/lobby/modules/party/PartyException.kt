package space.chunks.lobby.modules.party

enum class PartyExceptionReason{
    PARTY_GONE,
    INVITE_GONE,
    NOT_OWNER,
    INVITER_IS_INVITEE
}

class PartyException(val reason: PartyExceptionReason) : RuntimeException()