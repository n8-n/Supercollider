MpkMini3 {
	var <>knobCC, <>bankCC, <>knobScale, <>relativeKnobs, <>volumeKnob, <>currentBank, <>buses;
	var <>debugLogs = false;
	var <>server;


	*new {
		arg knobCC = #[70, 71, 72, 73, 74, 75, 76, 77], bankCC = #[16, 17, 18, 19, 20, 21, 22, 23],
		knobScale = 1, relativeKnobs = true, volumeKnob = true;

		^super.new.init(knobCC, bankCC, knobScale, relativeKnobs, volumeKnob);
	}


	init {
		arg knobCC, bankCC, knobScale, relativeKnobs, volumeKnob;

		this.knobCC = knobCC;
		this.bankCC = bankCC;
		this.knobScale = knobScale;
		this.relativeKnobs = relativeKnobs;
		this.volumeKnob = volumeKnob;
		this.currentBank = bankCC[0];
		this.buses = Dictionary.new();
		this.server = Server.default;

		this.server.waitForBoot({
			this.prConnectMidi("MPK mini 3", "MPK mini 3 MIDI 1");
		});

		this.prInitBuses();
		this.prInitMidiDefs();

		postln("MPK Mini3 initialised");
	}


	setKnobScale { | bank, knob, scale |
		this.buses[bank][knob][\scale] = scale;
	}


	getBus { | bank, knob |
		var finalKnob = this.knobCC[this.knobCC.size - 1];

		if (knob == finalKnob && this.volumeKnob) {
			warn("Final knob is reserved for master volume control");
		} {
			^this.buses[bank][knob][\bus];
		}
	}


	relativeKnobsMode { | enable = true |
		this.relativeKnobsMode = enable;
	}


	prIncrementer { |val, cc|
		var bus = this.buses[currentBank][cc][\bus];
		var toAdd = this.buses[currentBank][cc][\scale];

		var busAdd = { |n|
			var curr = bus.getSynchronous();
			var sum = curr + n;

			if (this.debugLogs, {
				postf("bank: %, bus: %, val: %\n", this.currentBank, cc, sum);
			});

			bus.setSynchronous(sum);
		};

		if(val == 1,
			{ busAdd.(toAdd) },
			{ busAdd.(toAdd.neg) }
		);
	}


	prInitBuses {
		// Create the buses dictionary.
		// Each bank has its own dict of knobs. Knobs are event objects.
		this.bankCC.do({ |bank, i|
			var bankDict = Dictionary.new();
			this.knobCC.do({ |kCc, j|
				var knob = ();
				knob[\scale] = knobScale;
				// Should server be configurable?
				knob[\bus] = Bus.control(this.server, 1).set(0); // default value = 0
				bankDict.put(kCc, knob);
			});

			this.buses.put(bank, bankDict);
		});
	}


	prInitMidiDefs {
		MIDIdef.cc(\MpkMini3_Knobs, { |val, num, chan, src|
			this.prIncrementer(val, num);
		}, ccNum: this.knobCC, chan: 0);


		MIDIdef.cc(\MpkMini3_Pads, { |val, num, chan, src|
			// Filter MIDI off values.
			if (val != 0) {
				if (this.debugLogs, {
					postf("Pads: %\n", [val, num, chan, src]);
				});
				this.currentBank = num;
			};
		}, ccNum: this.bankCC, chan: 9);


		// Set final knob to control global volume.
		// Applies for all banks.
		if (this.volumeKnob) {
			MIDIdef.cc(\MpkMini3_Volume, { |val, num, chan, src|
				var currentVolume = this.server.volume.volume;
				var toAdd = 0.1;

				var volumeAdd = { |n|
					{ this.server.volume.volume = currentVolume + n }.defer;
				};

				if(val == 1,
					{ volumeAdd.(toAdd) },
					{ volumeAdd.(toAdd.neg) }
				);
			}, ccNum: this.knobCC[this.knobCC.size - 1]);

		};
	}


	prConnectMidi { | device, name |
		if(MIDIClient.initialized == false) {
			MIDIClient.init;
		};

		MIDIClient.sources.do({ arg endPoint;
			if(device.notNil
				and: { name.notNil}
				and: {endPoint.device == device}
				and: { endPoint.name == name }) {
				// catch exception thrown when already connected
				try {
					// connect MIDI device to SuperCollider in port 0
					MIDIIn.connect(0, endPoint.uid);
				}
			}
		})
	}
}