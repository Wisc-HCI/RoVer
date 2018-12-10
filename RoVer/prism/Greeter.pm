mdp

global robot_greeted: bool;
global Greet_with_speech: bool;
global Greet_with_handshake: bool;
global Wait_for_response: bool;

init
	robot_greeted=false &
	Greet_with_speech=true &
	Greet_with_handshake=false &
	Wait_for_response=true &
	st_m=3 &
	(st_n=2 | st_n=0) &
	st_o=1 &
	st_p=1 &
	st_q=0
endinit
module Robot

	st_m: [0..3];
	[h_donespeak] st_m=0 -> (st_m'=1) & (robot_greeted'=false);
	[human_ignores] st_m=0 -> (st_m'=1) & (robot_greeted'=false);
	[end_greet] st_m=2 & Wait_for_response=true -> (st_m'=0) & (robot_greeted'=true);
	[end_greet] st_m=2 & Wait_for_response=false -> (st_m'=1) & (robot_greeted'=true);
	[begin_greet] st_m=3 -> (st_m'=2);

endmodule

module Human

	st_n: [0..2];
	[h_speak] st_n=0 & Wait_for_response=true & robot_greeted=true -> (st_n'=1);
	[human_ignores] st_n=0 & Wait_for_response=true -> (st_n'=0);
	[human_ignores] st_n=2 & Wait_for_response=true -> (st_n'=0);
	[h_speak] st_n=2 & Wait_for_response=true & robot_greeted=true -> (st_n'=1);

endmodule

module H_Speech

	st_o: [0..1];
	[h_donespeak] st_o=0 -> (st_o'=1);
	[h_speak] st_o=1 -> (st_o'=0);

endmodule

module R_Speech

	st_p: [0..1];
	[end_greet] st_p=0 -> (st_p'=1);
	[end_greet] st_p=1 -> (st_p'=1);
	[begin_greet] st_p=1 & Greet_with_speech=true -> (st_p'=0);
	[begin_greet] st_p=1 & Greet_with_speech=false -> (st_p'=1);

endmodule

module R_Arm

	st_q: [0..1];
	[begin_greet] st_q=0 & Greet_with_handshake=true -> (st_q'=1);
	[begin_greet] st_q=0 & Greet_with_handshake=false -> (st_q'=0);
	[end_greet] st_q=1 -> (st_q'=0);
	[end_greet] st_q=0 -> (st_q'=0);

endmodule

