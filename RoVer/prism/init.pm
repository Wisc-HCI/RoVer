mdp

global Speechhuman: bool;

init
	Speechhuman=false &
	(st_m=0 | st_m=1 | st_m=2)
endinit
module Wait

	st_m: [0..7];
	[] st_m=0 & Speechhuman=false -> (st_m'=3) & (Speechhuman'=true);
	[] st_m=0 -> (st_m'=4);
	[] st_m=1 & Speechhuman=false -> (st_m'=3) & (Speechhuman'=true);
	[] st_m=1 -> (st_m'=5);
	[] st_m=2 & Speechhuman=false -> (st_m'=3) & (Speechhuman'=true);
	[] st_m=2 -> (st_m'=6);
	[] st_m=3 -> (st_m'=7) & (Speechhuman'=false);
	[] st_m=4 -> (st_m'=4);
	[] st_m=5 -> (st_m'=5);
	[] st_m=6 -> (st_m'=6);
	[] st_m=7 -> (st_m'=7);

endmodule

