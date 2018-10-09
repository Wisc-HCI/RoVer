mdp

global Speechrobot: bool;
global Speechhuman: bool;
global Armrobot: bool;

init
	Speechrobot=false &
	Speechhuman=false &
	Armrobot=false &
	st_m=0 &
	(st_n=4 | st_n=5) &
	st_o=0
endinit
module Ask

	st_m: [0..8];
	[] st_m=0 & Speechrobot=false -> (st_m'=1) & (Speechrobot'=true);
	[] st_m=1 -> (st_m'=7) & (Speechrobot'=false);
	[] st_m=2 -> (st_m'=5) & (Speechhuman'=false);
	[] st_m=2 -> (st_m'=4) & (Speechhuman'=false);
	[] st_m=3 -> (st_m'=7) & (Speechrobot'=false);
	[] st_m=4 & Speechrobot=false -> (st_m'=3) & (Speechrobot'=true);
	[] st_m=5 -> (st_m'=6);
	[] st_m=6 -> (st_m'=6);
	[] st_m=7 & Speechhuman=false -> (st_m'=2) & (Speechhuman'=true);
	[] st_m=7 -> (st_m'=8);
	[] st_m=8 -> (st_m'=8);

endmodule

module Farewell

	st_n: [0..8];
	[] st_n=0 -> (st_n'=0);
	[] st_n=1 -> (st_n'=1);
	[] st_n=2 -> (st_n'=6) & (Speechrobot'=false);
	[] st_n=3 -> (st_n'=8) & (Speechrobot'=false);
	[] st_n=4 & Speechrobot=false -> (st_n'=2) & (Speechrobot'=true);
	[] st_n=5 & Speechrobot=false -> (st_n'=3) & (Speechrobot'=true);
	[] st_n=6 & Speechhuman=false -> (st_n'=7) & (Speechhuman'=true);
	[] st_n=6 -> (st_n'=0);
	[] st_n=7 -> (st_n'=1) & (Speechhuman'=false);
	[] st_n=8 & Speechhuman=false -> (st_n'=7) & (Speechhuman'=true);
	[] st_n=8 -> (st_n'=0);

endmodule

module Handoff

	st_o: [0..5];
	[] st_o=0 & Armrobot=false -> (st_o'=1) & (Armrobot'=true);
	[] st_o=1 -> (st_o'=3) & (Armrobot'=false);
	[] st_o=1 -> (st_o'=2);
	[] st_o=2 -> (st_o'=4);
	[] st_o=3 -> (st_o'=3);
	[] st_o=4 -> (st_o'=5) & (Armrobot'=false);
	[] st_o=5 -> (st_o'=5);

endmodule

