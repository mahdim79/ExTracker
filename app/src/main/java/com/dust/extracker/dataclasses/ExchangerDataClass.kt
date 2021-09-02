package com.dust.extracker.dataclasses

data class ExchangerDataClass(
    var name: String,
    var year_established: Int,
    var country: String,
    var url: String,
    var imageUrl: String,
    var trust_score: Int,
    var trust_score_rank: Int,
    var trade_volume_24h_btc: Double
) {
}