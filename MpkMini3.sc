MpkMini3 {
	var <knobCC, <bankCC, <knobScale, relativeKnobs, volumeKnob, currentBank, buses;
	var debugLogs = false;


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
				knob[\tag] = "Bank%_Knob%".format(i, j).asSymbol; // default tag
				bankDict.put(j.asSymbol, knob);
			});

			this.buses.put(i.asSymbol, bankDict);
		});
	}


	debugLogs { | enable = true |
		this.debugLogs = enable;
	}


	setKnobScale { | bank, knob, scale |
		this.buses[bank][knob][\scale] = scale;
	}


	tagBank { | bank, tag |

	}


	tagKnob { | bank, knob, tag |
		this.buses[bank][knob][\tag] = tag;
	}


	getBus { | bank, knob, asKr = true |
		if (knob == knobCC.size - 1 && volumeKnob) {
			warn("Final knob is reserved for master volume control");
		} {
			if (asKr) {
				^this.buses[bank][knob][\bus].kr;
			} {
				^this.buses[bank][knob][\bus];
			}
		}
	}


	relativeKnobsMode { | enable = true |
		this.relativeKnobsMode = enable;
	}
}