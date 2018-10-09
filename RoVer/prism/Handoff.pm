mdp

global end: bool;
global done: bool;

init
	end=false &
	done=false &
	st_m=0 &
	st_n=0 &
	st_o=1
endinit
module Robot

	st_m: [0..3];
	[extend] st_m=0 -> (st_m'=1);
	[contact] st_m=1 -> (st_m'=2);
	[timeout] st_m=1 -> (st_m'=3);
	[retract] st_m=2 & done=true -> (st_m'=3) & (end'=true);

endmodule

module Human

	st_n: [0..2];
	[contact] st_n=0 -> (st_n'=1);
	[] st_n=1 & end=false -> (st_n'=0) & (done'=true);
	[timeout] st_n=0 -> (st_n'=2);

endmodule

module R_Arm

	st_o: [0..1];
	[extend] st_o=1 -> (st_o'=0);
	[retract] st_o=0 -> (st_o'=1);
	[timeout] st_o=0 -> (st_o'=1);

endmodule

