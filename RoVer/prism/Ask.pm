mdp

global asked: bool;
global answered: bool;
global unknownAnswered: bool;

init
	asked=false &
	answered=false &
	unknownAnswered=false &
	st_m=0 &
	st_n=0 &
	st_o=1 &
	st_p=1
endinit
module Robot

	st_m: [0..4];
	[ask] st_m=0 -> (st_m'=1);
	[doneAsk] st_m=1 -> (st_m'=2) & (asked'=true);
	[ask] st_m=2 & unknownAnswered=true -> (st_m'=3) & (unknownAnswered'=false);
	[doneAsk] st_m=3 -> (st_m'=2) & (asked'=true);
	[] st_m=2 & answered=true & unknownAnswered=false -> (st_m'=4);
	[timeout] st_m=2 & answered=false & unknownAnswered=false -> (st_m'=4);

endmodule

module Human

	st_n: [0..2];
	[h_speak] st_n=0 & asked=true -> (st_n'=1) & (asked'=false);
	[h_donespeak] st_n=1 -> (st_n'=0) & (answered'=true);
	[h_donespeak] st_n=1 -> (st_n'=0) & (unknownAnswered'=true);
	[timeout] st_n=0 -> (st_n'=2);

endmodule

module R_Speech

	st_o: [0..1];
	[ask] st_o=1 -> (st_o'=0);
	[doneAsk] st_o=0 -> (st_o'=1);

endmodule

module H_Speech

	st_p: [0..1];
	[h_speak] st_p=1 -> (st_p'=0);
	[h_donespeak] st_p=0 -> (st_p'=1);

endmodule

