package space.chunks.lobby.modules.party

enum class PartyExceptionReason{
    PARTY_GONE,
    INVITE_GONE,
    INVITEE_ALREADY_PARTIED,
    NOT_OWNER,
}

class PartyException(val reason: PartyExceptionReason) : RuntimeException()