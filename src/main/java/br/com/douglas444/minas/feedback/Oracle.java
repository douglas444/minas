package br.com.douglas444.minas.feedback;

import br.com.douglas444.mltk.datastructure.Sample;

class Oracle {

    static Integer label(final Sample sample) {
        return sample.getY();
    }

}
