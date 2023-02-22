// in ./src/main/scala/aias_lab5/Hw4/InstMem.scala
package aias_lab5.Hw4

import chisel3._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

// IMEM
class InstMem extends Module {
  val io = IO(new Bundle {
    // val raddr = Input(UInt(5.W)) // memory 32層，可以用2^5=32即可
    val raddr = Input(UInt(7.W)) // 類似stack pointer概念，範圍0-127
    val rdata = Output(UInt(32.W)) //instruction
  })
  val memory = Mem(32, UInt(32.W)) // 32層
  // 用Binary的格式讀取位於src/main/resource/InstMem.txt作為Initial Value。
  loadMemoryFromFile(memory, "./src/main/resource/InstMem.txt",MemoryLoadFileType.Binary)

  io.rdata := memory((io.raddr>>2))
}