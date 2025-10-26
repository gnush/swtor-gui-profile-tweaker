package io.github.gnush.profiletweaker.data

import os.Path

case class CharacterGuiStateItem(path: Path, server: Server, characterName: String) {
  override def toString: String = s"[${server.name}] $characterName"
}