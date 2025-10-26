package io.github.gnush.profiletweaker.data

enum Server(val name: String):
  case TulakHord extends Server("Tulak Hord")
  case DarthMalgus extends Server("Darth Malgus")
  case StarForge extends Server("Star Forge")
  case SateleShan extends Server("Satele Shan")
  case ShaeVizla extends Server("Shae Vizla")
  case TheLeviathan extends Server("The Leviathan")

object Server:
  val fromId: PartialFunction[String, Server] = {
    case "he3000" => StarForge
    case "he3001" => SateleShan
    case "he4000" => DarthMalgus
    case "he4001" => TulakHord
    case "he4002" => TheLeviathan
    case "he8000" => ShaeVizla
  }