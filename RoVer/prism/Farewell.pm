mdp

global robot_goodbye: bool;

init
	robot_goodbye=false &
	st_m=3 &
	(st_n=2 | st_n=0) &
	st_o=1 &
	st_p=1
endinit
module Robot

	st_m: [0..3];
	[human_input] st_m=1 -> (st_m'=0) & (robot_goodbye'=false);
	[h_donespeak] st_m=1 -> (st_m'=0) & (robot_goodbye'=false);
	[doneSpeak] st_m=2 -> (st_m'=1) & (robot_goodbye'=true);
	[speak] st_m=3 -> (st_m'=2);

endmodule

module Human

	st_n: [0..2];
	[h_speak] st_n=0 & robot_goodbye=true -> (st_n'=1);
	[human_input] st_n=0 -> (st_n'=0);
	[human_input] st_n=2 -> (st_n'=0);
	[h_speak] st_n=2 & robot_goodbye=true -> (st_n'=1);

endmodule

module H_Speech

	st_o: [0..1];
	[h_donespeak] st_o=0 -> (st_o'=1);
	[h_speak] st_o=1 -> (st_o'=0);

endmodule

module R_Speech

	st_p: [0..1];
	[doneSpeak] st_p=0 -> (st_p'=1);
	[speak] st_p=1 -> (st_p'=0);

endmodule

