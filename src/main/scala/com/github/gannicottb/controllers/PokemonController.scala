package com.github.gannicottb.controllers
import com.github.gannicottb.model._
import zhttp.http.Response

object PokemonController {
  def random = Pokemon.random.map { p => Response.text(p.toString) }
}
