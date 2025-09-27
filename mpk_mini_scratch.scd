

MIDIIn.connectAll;
s.newBusAllocators;


MIDIFunc.trace(false);



// BANKS
(
~bank = 16;

MIDIdef.cc(\pads, {
	|val, num, chan, src|

	// Filter MIDI off values, i.e. return from function.
	if (val == 0) { ^val };

	if (~debugLogs, {
		format("%", [val, num, chan, src]).postln;
	});
	~bank = num;
}, ccNum: (16..23), chan: 9);


)




// KNOBS
(
~debugLogs = true;

~ccList = (70..76);
~scaleList = [50] ++ 1.dup(~ccList.size - 1);
~defaultVals = 0.dup(~ccList.size);

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
			postf("bank: %, bus: %, val: %\n", ~bank, cc, sum);
		});

		bus.setSynchronous(sum);
	};

	if(val == 1,
		{ busAdd.(add) },
		{ busAdd.(add.neg) }
	);
};


MIDIdef.cc(\knobs, { |val, num, chan, src|
	~inc_dec.(val, num);
	//postf("%\n", [val, num, chan, src]);
}, ccNum: ~ccList, chan: 0);

)


MIDIdef.all;




(
p = {
	var in = ~knobs[70][\bus].kr;
	in.wrap(-100, 100).poll(20) ;
}.play;
)


p = { b.poll(20) }.play;

(
p.free;
b.set(0);
)




// Install new class files here
Platform.userExtensionDir;


a = MpkMini3.new;
a.debugLogs_ = true;
b = a.getBus(16, 70);

b.set(0);

s.plotTree;
s.volume.gui;

#[1, 2, 3].size - 1

(
MIDIdef.cc(\volume, {
	|val, num, chan, src|

	var currentVolume = s.volume.volume;
	var add = 0.1;

	var volumeAdd = { |n|
		{ s.volume.volume = currentVolume + n }.defer;
	};

	if(val == 1,
		{ volumeAdd.(add) },
		{ volumeAdd.(add.neg) }
	);


}, ccNum: 77);

)


d = Dictionary();
d[16] = 122

d[16]





