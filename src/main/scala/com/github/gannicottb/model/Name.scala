package com.github.gannicottb.model

object Name {
  // taken from https://www.fantasynamegenerators.com/pokemon-names.php
  val default = List(
    "Flamitine",
    "Dragotle",
    "Hippopama",
    "Mantoth",
    "Chimezelle",
    "Golemadillo",
    "Gothida",
    "Quickecta",
    "Porcupid",
    "Hippony",
    "Salamalord",
    "Albalord",
    "Leopite",
    "Vulteaf",
    "Blastrilla",
    "Kineron",
    "Ninjoth",
    "Moltadillo",
    "Cheeturbo",
    "Frogre",
    "Elapet",
    "Pigeopip",
    "Komodius",
    "Pandou",
    "Fiequito",
    "Spiguin",
    "Electroth",
    "Magmela",
    "Rabbite",
    "Zebrawl"
  )

  def random = zio.Random.shuffle(default).map(_.head)
}
