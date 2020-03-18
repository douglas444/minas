package br.com.douglas444.minas.feedback;

import br.com.douglas444.mltk.Sample;

class Oracle {

    static Integer label(Sample sample) {
        return sample.getY();
    }

}
