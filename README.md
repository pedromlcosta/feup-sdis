# feup-sdis

1 thread listener sempre a correr<br>
varios threads que podem ser lançados para correr sub-protocolos (cmd)<br>
1 classe por cada sub-protocolo<br>
main chama os sub-protcolos com server ID<br>
verificar mensagens veem com o mesmo server ID (se sim, ignorar)<br>
ChunkID:<br>
FileiD SHA256<br>
Nchunk <br>

<br>
Stored Info:<br>
Data bytes de info<br>
ChunkID<br>
Replicação Atual<br>
Replicação Minima<br>
Array -> ServerID<br>
