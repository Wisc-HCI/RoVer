mdp

global Speechhuman: bool;
global Speechrobot: bool;

init
	Speechhuman=false &
	Speechrobot=false &
	(st_m=4 | st_m=5) &
	st_n=0
endinit
module Farewell

	st_m: [0..8];
	[] st_m=0 -> (st_m'=0);
	[] st_m=1 -> (st_m'=1);
	[] st_m=2 -> (st_m'=6) & (Speechrobot'=false);
	[] st_m=3 -> (st_m'=8) & (Speechrobot'=false);
	[] st_m=4 & Speechrobot=false -> (st_m'=2) & (Speechrobot'=true);
	[] st_m=5 & Speechrobot=false -> (st_m'=3) & (Speechrobot'=true);
	[] st_m=6 & Speechhuman=false -> (st_m'=7) & (Speechhuman'=true);
	[] st_m=6 -> (st_m'=0);
	[] st_m=7 -> (st_m'=1) & (Speechhuman'=false);
	[] st_m=8 & Speechhuman=false -> (st_m'=7) & (Speechhuman'=true);
	[] st_m=8 -> (st_m'=0);

endmodule

module Ask

	st_n: [0..8];
	[] st_n=0 & Speechrobot=false -> (st_n'=1) & (Speechrobot'=true);
	[] st_n=1 -> (st_n'=7) & (Speechrobot'=false);
	[] st_n=2 -> (st_n'=5) & (Speechhuman'=false);
	[] st_n=2 -> (st_n'=4) & (Speechhuman'=false);
	[] st_n=3 -> (st_n'=7) & (Speechrobot'=false);
	[] st_n=4 & Speechrobot=false -> (st_n'=3) & (Speechrobot'=true);
	[] st_n=5 -> (st_n'=6);
	[] st_n=6 -> (st_n'=6);
	[] st_n=7 & Speechhuman=false -> (st_n'=2) & (Speechhuman'=true);
	[] st_n=7 -> (st_n'=8);
	[] st_n=8 -> (st_n'=8);

endmodule

