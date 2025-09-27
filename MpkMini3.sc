MpkMini3 {
	var <>knobCC, <>bankCC, <>knobScale, <>relativeKnobs, <>volumeKnob, <>currentBank, <>buses;
	var <>debugLogs = true;


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

		// Create the buses dictionary.
		// Each bank has its own dict of knobs. Knobs are event objects.
		bankCC.do({ |bank, i|
			var bankDict = Dictionary.new();
			knobCC.do({ |kCc, j|
				var knob = ();
				knob[\scale] = knobScale;
				// Should server be configurable?
				knob[\bus] = Bus.control(Server.default, 1).set(0); // default value = 0
				//knob[\tag] = "Bank%_Knob%".format(i, j).asSymbol; // default tag
				bankDict.put(kCc, knob);
			});

			this.buses.put(bank, bankDict);
		});


		MIDIdef.cc(\MpkMini3_Knobs, { |val, num, chan, src|
			this.prIncrementer(val, num);
		}, ccNum: this.knobCC, chan: 0);


		MIDIdef.cc(\MpkMini3_Pads, { |val, num, chan, src|
			// Filter MIDI off values, i.e. return from function.
			if (val != 0) {
				if (this.debugLogs, {
					postf("Pads: %\n", [val, num, chan, src]);
				});
				this.currentBank = num;
			};
		}, ccNum: this.bankCC, chan: 9);

		postln("MPK Mini3 initialised");
	}


	setKnobScale { | bank, knob, scale |
		this.buses[bank][knob][\scale] = scale;
	}


	/*tagBank { | bank, tag |

	}*/


	/*tagKnob { | bank, knob, tag |
		this.buses[bank][knob][\tag] = tag;
	}*/


	getBus { | bank, knob |
		if (knob == (this.knobCC.size) && this.volumeKnob) {
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
}