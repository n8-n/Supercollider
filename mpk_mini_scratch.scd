

MIDIIn.connectAll;
s.newBusAllocators;


MIDIFunc.trace(false);



// BANKS
(
~bank = 0;

MIDIdef.cc(\pads, {
	|val, num, chan, src|

	// Filter MIDI off values, i.e. return from function.
	if (val == 0) { ^val };

	format("%", [val, num, chan, src]).postln;
	~bank = num;
	~bank.postln;
}, ccNum: (16..23), chan: 9);


)


~ccList +++ (~scaleList ++ Array.fill(1)) +++ ~defaultVals;

[1!2, 50, 1!3].flatten


[1, 2] +.x [2, 3]

// KNOBS
(
~debugLogs = true;

~ccList = (70..77);
~scaleList = [50, 1];
~defaultVals = 0;

~knobs = Dictionary();
~ccList.do({ |cc, i|
	var params = ();
	params[\scale] = ~scaleList[i];
	params[\bus] = Bus.control(s, 1).set(~defaultVals[i]);
	~knobs.put(cc, params);
});

~inc_dec = { |val, cc|
	var bus = ~knobs[cc][\bus];
	var add = ~knobs[cc][\scale];

	var busAdd = { |n|
		var curr = bus.getSynchronous();
		var sum = curr + n;

		if (~debugLogs, {
			format("bank: %, bus: %, val: %", ~bank, cc, sum).postln;
		});

		bus.setSynchronous(sum);
	};

	if(val == 1,
		{ busAdd.(add) },
		{ busAdd.(add.neg) }
	);
};


~count = 0;
MIDIdef.cc(\test, {
	|val, num, chan, src|

	~inc_dec.(val, num);
	//format("%", [val, num, chan, src]).postln;
}, ccNum: ~ccList, chan: 0);

)






(
p = {
	var in = ~knobs[70][\bus].kr;
	in.wrap(-100, 100).poll(20) ;
}.play;
)


p = { ~knobs[70][\bus].kr.poll(20) }.play;

(
p.free;
~knobs[70][\bus].set(0);
)




// Install new class files here
Platform.userExtensionDir;




a = MpkMini3.new;
a.test2;
