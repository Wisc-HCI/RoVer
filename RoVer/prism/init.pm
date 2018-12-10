mdp

global Speechhuman: bool;
global Speechrobot: bool;
global Armrobot: bool;

init
	Speechhuman=false &
	Speechrobot=false &
	Armrobot=false &
	(st_m=4 | st_m=5)
endinit
module Greeter

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

