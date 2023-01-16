package com.vitorpamplona.amethyst.service

import com.vitorpamplona.amethyst.model.LocalCache
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.model.User
import nostr.postr.JsonFilter
import nostr.postr.events.MetadataEvent
import nostr.postr.events.TextNoteEvent

object NostrUserProfileDataSource: NostrDataSource<Note>("UserProfileFeed") {
  var user: User? = null

  fun loadUserProfile(userId: String) {
    user = LocalCache.users[userId]
    resetFilters()
  }

  fun createUserInfoFilter(): JsonFilter {
    return JsonFilter(
      kinds = listOf(MetadataEvent.kind),
      authors = listOf(user!!.pubkeyHex),
      since = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 7)
    )
  }

  fun createUserPostsFilter(): JsonFilter {
    return JsonFilter(
      kinds = listOf(TextNoteEvent.kind),
      authors = listOf(user!!.pubkeyHex),
      since = System.currentTimeMillis() / 1000 - (60 * 60 * 24 * 4)
    )
  }

  val userInfoChannel = requestNewChannel()
  val notesChannel = requestNewChannel()

  override fun feed(): List<Note> {
    return user?.notes?.sortedBy { it.event?.createdAt }?.reversed() ?: emptyList()
  }

  override fun updateChannelFilters() {
    userInfoChannel.filter = createUserInfoFilter()
    notesChannel.filter = createUserPostsFilter()
  }
}