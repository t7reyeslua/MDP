clf;       
Xaxis=10;
        Yaxis=inf;
        figure(1);
        hold on;
        
        subplot(3,1,1)
        plot(plotfreq,powerFFTX,'b');
        axis([0,Xaxis,0,Yaxis])
        title('fft Acc X');
             hx = graph2d.constantline((FundfreqX), 'LineStyle',':', 'Color','b');
             changedependvar(hx,'x');
        
        subplot(3,1,2)
        plot(plotfreq,powerFFTY,'r');
        axis([0,Xaxis,0,Yaxis])
        title('fft Acc Y');
        hx = graph2d.constantline((FundfreqY), 'LineStyle',':', 'Color','r');
             changedependvar(hx,'x');
        
        subplot(3,1,3)
        plot(plotfreq,powerFFTZ,'green');
        axis([0,Xaxis,0,Yaxis])
        title('fft Acc Z');
        hx = graph2d.constantline((FundfreqZ), 'LineStyle',':', 'Color','green');
             changedependvar(hx,'x');
        
        hold off;