mdp

global Speechrobot: bool;
global Speechhuman: bool;

init
	Speechrobot=false &
	Speechhuman=false &
	(st_m=1 | st_m=2 | st_m=3)
endinit
module Answer

	st_m: [0..17];
	[] st_m=0 & Speechhuman=false -> (st_m'=10) & (Speechhuman'=true);
	[] st_m=1 -> (st_m'=4);
	[] st_m=2 -> (st_m'=5);
	[] st_m=3 -> (st_m'=6);
	[] st_m=4 -> (st_m'=7);
	[] st_m=4 & Speechhuman=false -> (st_m'=11) & (Speechhuman'=true);
	[] st_m=5 -> (st_m'=8);
	[] st_m=5 & Speechhuman=false -> (st_m'=11) & (Speechhuman'=true);
	[] st_m=6 -> (st_m'=9);
	[] st_m=6 & Speechhuman=false -> (st_m'=11) & (Speechhuman'=true);
	[] st_m=7 -> (st_m'=7);
	[] st_m=8 -> (st_m'=8);
	[] st_m=9 -> (st_m'=9);
	[] st_m=10 -> (st_m'=16) & (Speechhuman'=false);
	[] st_m=10 -> (st_m'=12) & (Speechhuman'=false);
	[] st_m=11 -> (st_m'=16) & (Speechhuman'=false);
	[] st_m=11 -> (st_m'=12) & (Speechhuman'=false);
	[] st_m=12 & Speechrobot=false -> (st_m'=13) & (Speechrobot'=true);
	[] st_m=12 & Speechrobot=false -> (st_m'=13) & (Speechrobot'=true);
	[] st_m=12 & Speechrobot=false -> (st_m'=14) & (Speechrobot'=true);
	[] st_m=13 -> (st_m'=0) & (Speechrobot'=false);
	[] st_m=14 -> (st_m'=15) & (Speechrobot'=false);
	[] st_m=15 -> (st_m'=15);
	[] st_m=16 & Speechrobot=false -> (st_m'=17) & (Speechrobot'=true);
	[] st_m=17 -> (st_m'=7) & (Speechrobot'=false);

endmodule

