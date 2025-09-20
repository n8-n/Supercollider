
/*(
~connect_midi_devices = { arg device, name;
    // init or refresh MIDIClient end points
    if(MIDIClient.initialized == false) {
        MIDIClient.init;
    } {
        MIDIClient.list;
    };

    MIDIClient.sources.do({ arg endPoint;
        if(device.notNil and: { name.notNil} and: {endPoint.device == device} and: { endPoint.name == name }) {
            // catch exception thrown when already connected
            try {
                // connect SuperCollider out port 0 to MIDI device
                MIDIOut.connect(0, endPoint.uid);
            };
            try {
                // connect MIDI device to SuperCollider in port 0
                MIDIIn.connect(0, endPoint.uid);
            }
        }
    })

}
)

~connect_midi_devices.("MPK mini 3", "MPK mini 3 MIDI 1");*/



MIDIIn.connectAll;
s.newBusAllocators;


MIDIFunc.trace(false);





(
s.waitForBoot({
	~ccList = [70, 71];
	~scaleList = [50, 1];
	~defaultVals = 0!2;

	~knobs = Dictionary();
	~ccList.do({ |cc, i|
		var params = ();
		params[\scale] = ~scaleList[i];
		params[\bus] = Bus.control(s, 1).set(~defaultVals[i]);
		~knobs.put(cc, params);
	});

	s.sync;

	~inc_dec = { |val, cc|
		var bus = ~knobs[cc][\bus];
		var add = ~knobs[cc][\scale];

		/*var busAdd = { |n|
			bus.getn(1, { |val|
				bus.set(val + n);
			});
		};*/

		var busAdd = { |n|
			var curr = bus.getSynchronous();
			bus.setSynchronous(curr + n);
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
		//format("%: %; val: %", ~count, [val, num, chan, src]).postln;
	}, ccNum: ~ccList, chan: 0);

});
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





