 function [fftArray] = fft_Graph(Array,SampleFreq) %Returns the array filtered 
            %Result is ready to plot
            [row,col] = size(Array);
            nfft=row;

                for i=(1:col)
                    fft_temp=fft(Array(:,i));
                    fft_temp(1)=[];
                    powerFFT = abs(fft_temp(1:floor(nfft/2))).^2;

                    fftArray(:,i)=powerFFT;
                end
                
         end