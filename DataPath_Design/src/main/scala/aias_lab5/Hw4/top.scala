package aias_lab5.Hw4

import chisel3._
import chisel3.util._

class top extends Module {
    val io = IO(new Bundle{
        val pc_out = Output(UInt(32.W))
        val alu_out = Output(UInt(32.W))
        val rf_wdata_out = Output(UInt(32.W))
        val brtaken_out = Output(Bool())
        val jmptaken_out = Output(Bool())
    })

    val pc = Module(new PC())
    val im = Module(new InstMem())
    val dc = Module(new Decoder())
    val rf = Module(new RegFile(2)) // rf(0) : rs1 // rf(1) : rs2
    val alu = Module(new ALU())
    val bc = Module(new BranchComp())
    val dm = Module(new DataMem())
    
    //PC
    pc.io.jmptaken := false.B // Don't modify
    pc.io.brtaken := false.B // Don't modify
    pc.io.offset := 0.U // Don't modify
    
    //Insruction Memory
    im.io.raddr := pc.io.pc
    
    //Decoder
    dc.io.inst := im.io.rdata
    
    //RegFile
    rf.io.raddr(0) := dc.io.rs1
    rf.io.raddr(1) := dc.io.rs2
    rf.io.wdata := MuxLookup(dc.io.ctrl_WBSel, 0.U,Seq(
        0.U -> dm.io.rdata.asUInt, // dmem rdata
        1.U -> alu.io.out,  // alu output
        2.U -> pc.io.pc, // pc + 4 FIXME:
    )) 
    
    rf.io.waddr := 0.U  // Don't modify
    rf.io.wen := false.B // Don't modify

    //ALU
    val rdata_or_zero = WireDefault(0.U(32.W))
    alu.io.src1 := Mux((dc.io.ctrl_ASel === 0.U), rf.io.rdata(0), pc.io.pc) // rf(0) : rs1
    alu.io.src2 := Mux((dc.io.ctrl_BSel === 1.U), dc.io.imm.asUInt, rf.io.rdata(1)) // rf(1) : rs2
    
    alu.io.funct3 := dc.io.funct3
    alu.io.funct7 := dc.io.funct7
    alu.io.opcode := dc.io.opcode
    
    //Data Memory input:　funct3 raddr wen waddr wdata , output: rdata 
    dm.io.funct3 := dc.io.funct3
    dm.io.raddr := alu.io.out
    dm.io.wen := dc.io.ctrl_MemRW
    dm.io.waddr := alu.io.out
    dm.io.wdata := rf.io.rdata(1)

    //Branch Comparator
    bc.io.en :=  dc.io.ctrl_Br 
    bc.io.funct3 := dc.io.funct3 // branch系列可根據funct3做區分
    bc.io.src1 := rf.io.rdata(0) // rs1
    bc.io.src2 := rf.io.rdata(1) // rs2

    //Check Ports
    io.pc_out := pc.io.pc
    io.alu_out := alu.io.out 
    io.rf_wdata_out := rf.io.wdata
    io.brtaken_out := bc.io.brtaken
    io.jmptaken_out := dc.io.ctrl_Jmp
}