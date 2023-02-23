%% 2. semestralni prace z predmetu MS1 
% Jan Burian

%%
clc
clear
close all 

%% Nacteni signalu 
load('signal.mat')

%% Zobrazeni casoveho vyvoje signalu (vzorkovaci frekvence je 80 kHz)
fs = 80000; % Hz
Ts = 1 / fs; % perioda vzorkovani 
t = length(signal) / fs; % doba trvani v sekundach

x = linspace(0, t, length(signal)); % vygenerovani prislusneho vektoru (za 1 sec 80000 vzorku 
                                    % => za dobu t t * 80000 vzorku)

figure
plot(x, signal);
xlabel('t [s]');
ylabel('x(t)');
grid on;
title('Zobrazeni casoveho vyvoje signalu');

%% Stredni hodnota signalu, energie signalu a efektivni hodnota
% Stredni hodnota signalu
stredni_hodnota_pomocna = 0; 
for i = 1:length(signal)
    stredni_hodnota_pomocna = stredni_hodnota_pomocna + signal(i,1); 
end 

stredni_hodnota = stredni_hodnota_pomocna / length(signal); 

% Energie signalu
energie_signalu_pomocna = 0;
for u = 1:length(signal)
    energie_signalu_pomocna = energie_signalu_pomocna + power(signal(u,1), 2);
end

energie_signalu = energie_signalu_pomocna * Ts;

% Efektivni hodnota
t = length(signal) / fs; % doba trvani v sekundach
vykon_signalu = (1/t) * energie_signalu;

efektivni_hodnota = sqrt(vykon_signalu); 

%% Amplitudove spektrum signalu
n = length(signal); % pocet vzorku
NFFT = 2^nextpow2(n);
X = fft(signal, NFFT) / n;
f = fs / 2*linspace(0, 1, NFFT/2+1);

plot(f,2*abs(X(1:NFFT/2+1))) 
title('Jednostranne amplitudove spektrum signalu');
xlabel('Frekvence [Hz]')
ylabel('|X(f)|')

%% Metoda kratkodobe Fourierovy transformace
[S, W, T] = stft(signal);

%% Princip neurcitosti
figure
win = hamming(256);
stft(signal,fs,'Window',win,'OverlapLength',128,'FFTLength',256);
title('Okenkova funkce: 256 vzorku')
ylabel('Frekvence [kHz]')
xlabel('t [s]')

figure
win = hamming(4096);
stft(signal,fs,'Window',win,'OverlapLength',2048,'FFTLength',8196);
title('Okenkova funkce: 4096 vzorku')
ylabel('Frekvence [kHz]')
xlabel('t [s]')

figure
win = hamming(8192);
stft(signal,fs,'Window',win,'OverlapLength',4096,'FFTLength',8192);
title('Okenkova funkce: 8192 vzorku')
ylabel('Frekvence [kHz]')
xlabel('t [s]')

%% Caso-frekvencni udalosti
figure
win = hamming(8192);
stft(signal,fs,'Window',win,'OverlapLength',4096,'FFTLength',8192);
title('Okenkova funkce: 8192 vzorku')
ylabel('Frekvence [kHz]')
xlabel('t [s]')
xline(4, '-.r')
xline(5, '-.r')
xline(14, '-.r')
xline(15, '-.r')

