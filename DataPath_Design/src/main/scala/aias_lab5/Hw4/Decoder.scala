package aias_lab5.Hw4

import chisel3._
import chisel3.util._

object opcode_map {
    val LOAD      = "b0000011".U    // Load opcode = 0000011
    val STORE     = "b0100011".U    // store opcode = 0100011
    val BRANCH    = "b1100011".U    // branch opcode = 1100011
    val JALR      = "b1100111".U    // jalr opcode = 1100111
    val JAL       = "b1101111".U    // jal opcode = 1101111
    val OP_IMM    = "b0010011".U    // I-Format opcode = 0010011
    val OP        = "b0110011".U    // op opcode = 0110011 (add,sub,...) R-format
    val AUIPC     = "b0010111".U    // AUIPC opcode = 0010111
    val LUI       = "b0110111".U    // lui opcode = 0110111
}

import opcode_map._

class Decoder extends Module{
    val io = IO(new Bundle{
        val inst = Input(UInt(32.W)) // instruction 32-bits

        //Please fill in the blanks by yourself
        val funct3 = Output(UInt(3.W)) // funct3 = inst[12-14]
        val funct7 = Output(UInt(7.W)) // funct7 = inst[25-31]
        val rs1 = Output(UInt(5.W)) // rs1 = inst[15-19]
        val rs2 = Output(UInt(5.W)) // rs2 = inst[20-24]
        val rd = Output(UInt(5.W)) // rd = inst[7-11]
        val opcode = Output(UInt(7.W)) // opcode = inst[0-6]
        val imm = Output(SInt(32.W)) // imm 由 immGen 擴充成 32 bit
        
        val ctrl_RegWEn = Output(Bool())  // for Reg write back
        val ctrl_ASel = Output(Bool()) // for alu src1 pc
        val ctrl_BSel = Output(Bool()) // for alu src2 imm
        val ctrl_Br = Output(Bool()) // for branch inst.
        val ctrl_Jmp = Output(Bool()) // for jump inst.
        val ctrl_Lui = Output(Bool()) // for lui inst.
        val ctrl_MemRW = Output(Bool()) // for L/S inst
        val ctrl_WBSel = Output(UInt(2.W)) // for write back select 3 type
    })

    //Please fill in the blanks by yourself
    io.funct3 := io.inst(14,12)
    io.funct7 := io.inst(31,25)
    io.rs1 := io.inst(19,15)
    io.rs2 := io.inst(24,20)
    io.rd := io.inst(11,7)
    io.opcode := io.inst(6,0)

    //ImmGen
    // core instruction formats
    io.imm := MuxLookup(io.opcode,0.S,Seq(
        //R-type no imm
        //Please fill in the blanks by yourself
        OP ->  0.S,

        //I-type
        OP_IMM -> io.inst(31,20).asSInt,
        
        JALR -> io.inst(31, 20).asSInt,
        
        LOAD -> io.inst(31, 20).asSInt,
        
        //B-type         imm[12],    imm[11],      imm[10,5],     imm[4,1],   imm[0]
        BRANCH -> (Cat(io.inst(31), io.inst(7), io.inst(30,25), io.inst(11,8), 0.U)).asSInt,

        //S-type
        STORE -> (Cat(io.inst(31,25), io.inst(11,7))).asSInt,

        //U-type
        AUIPC -> (io.inst(31,12) << 12).asSInt,

        LUI -> (io.inst(31,12) << 12).asSInt,
        
        //J-type
        JAL -> (Cat(io.inst(31), io.inst(19,12), io.inst(20), io.inst(30,21), 0.U)).asSInt,
        
    ))

    //Controller
    
    io.ctrl_RegWEn := MuxLookup(io.opcode, true.B, Seq(STORE -> false.B, BRANCH -> false.B)) // 不會寫回reg store, branch : false.B // other : true.B
    io.ctrl_ASel := MuxLookup(io.opcode, false.B, Seq(BRANCH -> true.B, JAL -> true.B, AUIPC -> true.B)) 
    // 有用到pc的 BRANCH、JAL、AUIPC、: true.B [jalr : pc+4(pc module會算好) or rs1 + imm 沒有用到pc]
    // other inst : false.B
    io.ctrl_BSel := Mux((io.opcode === OP),false.B, true.B) // r-type 沒有用到 imm :false.B , others : use imm : true.B 
    io.ctrl_Br := Mux((io.opcode === BRANCH), true.B, false.B) // branch : true.B // others: false.B
    io.ctrl_Jmp := MuxLookup(io.opcode, false.B, Seq(JALR -> true.B , JAL -> true.B)) // jump : jal、jalr: true.B // others : false.B
    io.ctrl_Lui :=  Mux((io.opcode === LUI), true.B, false.B) // lui : true.B 只看liu others : false.B
    io.ctrl_MemRW := Mux((io.opcode === STORE), true.B, false.B) // store : 去mem R or W : true.B // others : false.B
    io.ctrl_WBSel := MuxLookup(io.opcode, 1.U, Seq(LOAD -> 0.U, JALR -> 2.U, JAL -> 2.U))  // wb to reg
    // load: 0:dm data, others inst: 1:alu output, jalr, jal: 2:pc+4 
}