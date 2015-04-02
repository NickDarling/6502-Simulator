.data
$FFFE	 06	
$FFFF	 06

.code
$0600    00        BRK 
$0601    69 01     ADC #$01
$0603    4c 00 06  JMP $0600
$0606    40        RTI 