mdp

global Armrobot: bool;

init
	Armrobot=false &
	st_m=0
endinit
module Handoff

	st_m: [0..5];
	[] st_m=0 & Armrobot=false -> (st_m'=1) & (Armrobot'=true);
	[] st_m=1 -> (st_m'=3) & (Armrobot'=false);
	[] st_m=1 -> (st_m'=2);
	[] st_m=2 -> (st_m'=4);
	[] st_m=3 -> (st_m'=3);
	[] st_m=4 -> (st_m'=5) & (Armrobot'=false);
	[] st_m=5 -> (st_m'=5);

endmodule

